package com.travis.monolith.ops.errorlog.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.monolith.ops.errorlog.api.request.SysErrorLogPageReq;
import com.travis.monolith.ops.errorlog.internal.converter.SysErrorLogConverter;
import com.travis.monolith.ops.errorlog.internal.entity.SysErrorLog;
import com.travis.monolith.ops.errorlog.internal.mapper.SysErrorLogMapper;
import com.travis.monolith.ops.errorlog.internal.service.impl.SysErrorLogServiceImpl;
import java.time.LocalDateTime;
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
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldQueryByExceptionTimeIpAndRequestMethod() {
        var mapper = mock(SysErrorLogMapper.class);
        var service = new SysErrorLogServiceImpl(mock(SysErrorLogConverter.class));
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
                .contains("request_method =", "ip LIKE", "create_time >=", "create_time <=");
        assertThat(wrapper.getParamNameValuePairs().values())
                .contains("POST", "%192.168%", request.getStartTime(), request.getEndTime());
    }
}
