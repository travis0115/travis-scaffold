package com.travis.monolith.ops.errorlog.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.dev33.satoken.stp.StpLogic;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.satoken.core.StpKit;
import com.travis.monolith.ops.errorlog.api.request.SysErrorLogHandleReq;
import com.travis.monolith.ops.errorlog.api.request.SysErrorLogPageReq;
import com.travis.monolith.ops.errorlog.api.response.SysErrorLogResp;
import com.travis.monolith.ops.errorlog.internal.converter.SysErrorLogConverter;
import com.travis.monolith.ops.errorlog.internal.entity.SysErrorLog;
import com.travis.monolith.ops.errorlog.internal.entity.SysErrorLogOccurrence;
import com.travis.monolith.ops.errorlog.internal.mapper.SysErrorLogMapper;
import com.travis.monolith.ops.errorlog.internal.mapper.SysErrorLogOccurrenceMapper;
import com.travis.monolith.ops.errorlog.internal.service.impl.SysErrorLogServiceImpl;
import com.travis.monolith.system.user.api.SysUserApi;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class SysErrorLogServiceImplTest {

    @BeforeAll
    static void initializeTableInfo() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""), SysErrorLog.class);
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                SysErrorLogOccurrence.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldQueryByExceptionTimeIpAndRequestMethod() {
        var mapper = mock(SysErrorLogMapper.class);
        var userApi = mock(SysUserApi.class);
        when(userApi.getUsernameMapByIds(any())).thenReturn(java.util.Map.of());
        var service =
                new SysErrorLogServiceImpl(
                        mock(SysErrorLogOccurrenceMapper.class),
                        userApi,
                        mock(SysErrorLogConverter.class));
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        when(mapper.page(anyInt(), anyInt(), any(LambdaQueryWrapperX.class)))
                .thenReturn(new Page<>());
        var request = new SysErrorLogPageReq();
        request.setRequestMethod(" post ");
        request.setIp("192.168");
        request.setStartTime(LocalDateTime.of(2026, 8, 12, 10, 0));
        request.setEndTime(LocalDateTime.of(2026, 8, 12, 11, 0));

        service.page(request);

        var wrapperCaptor = ArgumentCaptor.forClass(LambdaQueryWrapperX.class);
        verify(mapper).page(anyInt(), anyInt(), wrapperCaptor.capture());
        var wrapper = wrapperCaptor.getValue();
        assertThat(wrapper.getSqlSegment())
                .contains(
                        "request_method =",
                        "ip LIKE",
                        "last_occurrence_time >=",
                        "last_occurrence_time <=");
        assertThat(wrapper.getParamNameValuePairs().values())
                .contains("POST", "%192.168%", request.getStartTime(), request.getEndTime());
        assertThat(wrapper.getSqlSelect())
                .contains(
                        "business_key",
                        "request_id",
                        "exception_class",
                        "message",
                        "last_occurrence_time")
                .doesNotContain(
                        "request_method",
                        "stack_trace",
                        "request_params",
                        "controller_method");
    }

    @Test
    void shouldPersistOccurrenceForUpsertedAggregate() {
        var mapper = mock(SysErrorLogMapper.class);
        var occurrenceMapper = mock(SysErrorLogOccurrenceMapper.class);
        var service =
                new SysErrorLogServiceImpl(
                        occurrenceMapper, mock(SysUserApi.class), mock(SysErrorLogConverter.class));
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        var errorLog = new SysErrorLog();
        errorLog.setFingerprint("fingerprint");
        errorLog.setFirstOccurrenceTime(LocalDateTime.of(2026, 8, 12, 12, 0));
        var occurrence = new SysErrorLogOccurrence();
        when(mapper.selectIdByFingerprint("fingerprint")).thenReturn(100L);

        service.record(errorLog, occurrence);

        verify(mapper).upsertAggregate(errorLog);
        var occurrenceCaptor = ArgumentCaptor.forClass(SysErrorLogOccurrence.class);
        verify(occurrenceMapper).insert(occurrenceCaptor.capture());
        assertThat(occurrenceCaptor.getValue().getErrorLogId()).isEqualTo(100L);
        assertThat(occurrenceCaptor.getValue().getId()).isNotNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldDeleteOccurrencesBeforeAggregate() {
        var mapper = mock(SysErrorLogMapper.class);
        var occurrenceMapper = mock(SysErrorLogOccurrenceMapper.class);
        var service =
                new SysErrorLogServiceImpl(
                        occurrenceMapper, mock(SysUserApi.class), mock(SysErrorLogConverter.class));
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        when(mapper.selectById(100L)).thenReturn(new SysErrorLog());

        service.delete(100L);

        var wrapperCaptor = ArgumentCaptor.forClass(LambdaQueryWrapperX.class);
        var ordered = inOrder(occurrenceMapper, mapper);
        ordered.verify(occurrenceMapper).delete(wrapperCaptor.capture());
        ordered.verify(mapper).deleteById(100L);
        assertThat(wrapperCaptor.getValue().getSqlSegment()).contains("error_log_id =");
        assertThat(wrapperCaptor.getValue().getParamNameValuePairs().values()).contains(100L);
    }

    @Test
    void shouldRejectAlreadyHandledErrorLog() {
        var mapper = mock(SysErrorLogMapper.class);
        var service =
                new SysErrorLogServiceImpl(
                        mock(SysErrorLogOccurrenceMapper.class),
                        mock(SysUserApi.class),
                        mock(SysErrorLogConverter.class));
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        var errorLog = new SysErrorLog();
        errorLog.setStatus(1);
        when(mapper.selectById(100L)).thenReturn(errorLog);
        var request = new SysErrorLogHandleReq();
        request.setStatus(1);

        assertThatThrownBy(() -> service.handle(100L, request))
                .hasMessageContaining("错误日志已处理");
        verify(mapper, never()).handleIfPending(any(), any(), any(), any(), any());
    }

    @Test
    void shouldRejectConcurrentRepeatedHandling() {
        var mapper = mock(SysErrorLogMapper.class);
        var service =
                new SysErrorLogServiceImpl(
                        mock(SysErrorLogOccurrenceMapper.class),
                        mock(SysUserApi.class),
                        mock(SysErrorLogConverter.class));
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        var errorLog = new SysErrorLog();
        errorLog.setStatus(0);
        when(mapper.selectById(100L)).thenReturn(errorLog);
        var request = new SysErrorLogHandleReq();
        request.setStatus(1);
        var logic = mock(StpLogic.class);
        when(logic.getLoginIdAsLong()).thenReturn(7L);

        try (var stpKit = mockStatic(StpKit.class)) {
            stpKit.when(() -> StpKit.of(LoginType.ADMIN)).thenReturn(logic);
            assertThatThrownBy(() -> service.handle(100L, request))
                    .hasMessageContaining("错误日志已处理");
        }
        verify(mapper).handleIfPending(any(), any(), any(), any(), any());
    }

    @Test
    void shouldHandleAllPendingErrorLogs() {
        var mapper = mock(SysErrorLogMapper.class);
        var service =
                new SysErrorLogServiceImpl(
                        mock(SysErrorLogOccurrenceMapper.class),
                        mock(SysUserApi.class),
                        mock(SysErrorLogConverter.class));
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        var request = new SysErrorLogHandleReq();
        request.setStatus(2);
        request.setRemark("  已确认无需处理  ");
        var logic = mock(StpLogic.class);
        when(logic.getLoginIdAsLong()).thenReturn(7L);
        when(mapper.handleAllPending(eq(2), eq(7L), any(), eq("已确认无需处理"))).thenReturn(3);

        try (var stpKit = mockStatic(StpKit.class)) {
            stpKit.when(() -> StpKit.of(LoginType.ADMIN)).thenReturn(logic);
            assertThat(service.handleAllPending(request)).isEqualTo(3);
        }
        verify(mapper).handleAllPending(eq(2), eq(7L), any(), eq("已确认无需处理"));
    }

    @Test
    void shouldAllowEmptyUserMapWhenHandlerIsNull() {
        var mapper = mock(SysErrorLogMapper.class);
        var userApi = mock(SysUserApi.class);
        var converter = mock(SysErrorLogConverter.class);
        var service =
                new SysErrorLogServiceImpl(
                        mock(SysErrorLogOccurrenceMapper.class), userApi, converter);
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        var entity = new SysErrorLog();
        var response = new SysErrorLogResp();
        response.setPlatformType("ADMIN");
        response.setUsername("travis0115");
        when(mapper.page(anyInt(), anyInt(), any(LambdaQueryWrapperX.class)))
                .thenReturn(new Page<SysErrorLog>().setRecords(List.of(entity)).setTotal(1));
        when(converter.toResp(entity)).thenReturn(response);
        when(userApi.getUsernameMapByIds(any())).thenReturn(Map.of());

        var result = service.page(new SysErrorLogPageReq());

        assertThat(result.getRecords()).containsExactly(response);
        assertThat(response.getHandledByUsername()).isNull();
    }
}
