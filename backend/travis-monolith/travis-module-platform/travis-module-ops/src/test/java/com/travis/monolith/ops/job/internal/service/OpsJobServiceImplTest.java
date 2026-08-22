package com.travis.monolith.ops.job.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.quartz.core.QuartzJobHandlerRegistry;
import com.travis.monolith.ops.job.api.enums.OpsJobStatus;
import com.travis.monolith.ops.job.api.request.OpsJobCreateReq;
import com.travis.monolith.ops.job.api.request.OpsJobPageReq;
import com.travis.monolith.ops.job.api.response.OpsJobPageResp;
import com.travis.monolith.ops.job.internal.converter.OpsJobConverter;
import com.travis.monolith.ops.job.internal.entity.OpsJob;
import com.travis.monolith.ops.job.internal.entity.OpsJobLog;
import com.travis.monolith.ops.job.internal.mapper.OpsJobMapper;
import com.travis.monolith.ops.job.internal.service.impl.OpsJobServiceImpl;
import com.travis.monolith.system.user.api.SysUserApi;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class OpsJobServiceImplTest {

    @AfterEach
    void clearSynchronization() {
        TransactionSynchronizationManager.setActualTransactionActive(false);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void shouldSynchronizeQuartzOnlyAfterTransactionCommit() {
        QuartzJobManager manager = mock(QuartzJobManager.class);
        OpsJobMapper mapper = mock(OpsJobMapper.class);
        OpsJob job = job();
        var service = service(manager, mapper);
        when(mapper.selectById(1001L)).thenReturn(job);
        when(mapper.updateById(job)).thenReturn(1);
        initTransactionSynchronization();

        service.changeStatus(1001L, OpsJobStatus.ENABLED.getValue());

        verify(manager, never()).schedule(job);
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(
                        synchronization -> {
                            synchronization.afterCommit();
                            synchronization.afterCompletion(
                                    TransactionSynchronization.STATUS_COMMITTED);
                        });
        verify(manager).schedule(job);
    }

    @Test
    void shouldNotSynchronizeQuartzAfterTransactionRollback() {
        QuartzJobManager manager = mock(QuartzJobManager.class);
        OpsJobMapper mapper = mock(OpsJobMapper.class);
        OpsJob job = job();
        var service = service(manager, mapper);
        when(mapper.selectById(1001L)).thenReturn(job);
        when(mapper.updateById(job)).thenReturn(1);
        initTransactionSynchronization();

        service.changeStatus(1001L, OpsJobStatus.ENABLED.getValue());
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(
                        synchronization ->
                                synchronization.afterCompletion(
                                        TransactionSynchronization.STATUS_ROLLED_BACK));

        verify(manager, never()).schedule(job);
    }

    @Test
    void shouldCompleteOnceOnlyForCurrentConfiguration() {
        QuartzJobManager manager = mock(QuartzJobManager.class);
        OpsJobMapper mapper = mock(OpsJobMapper.class);
        OpsJob job = job();
        job.setScheduleType("ONCE");
        job.setStatus(OpsJobStatus.ENABLED.getValue());
        var service = service(manager, mapper);
        when(mapper.selectById(1001L)).thenReturn(job);
        when(manager.configFingerprint(job)).thenReturn("current");

        service.completeOnce(1001L, "outdated");

        verify(mapper, never()).updateById(job);
    }

    @Test
    void shouldRejectBuiltinJobConfigurationChanges() {
        OpsJobMapper mapper = mock(OpsJobMapper.class);
        OpsJob job = job();
        job.setIsBuiltin(1);
        when(mapper.selectById(1001L)).thenReturn(job);
        var service = service(mock(QuartzJobManager.class), mapper);

        assertThatThrownBy(
                        () ->
                                service.update(
                                        1001L,
                                        mock(
                                                com.travis.monolith.ops.job.api.request
                                                        .OpsJobUpdateReq.class)))
                .hasMessageContaining("系统内置调度任务不允许修改");
        assertThatThrownBy(() -> service.delete(1001L)).hasMessageContaining("系统内置调度任务不允许修改");
        assertThatThrownBy(() -> service.copy(1001L)).hasMessageContaining("系统内置调度任务不允许修改");
        assertThatThrownBy(() -> service.changeStatus(1001L, OpsJobStatus.DISABLED.getValue()))
                .hasMessageContaining("系统内置调度任务不允许修改");
    }

    @Test
    void shouldRejectBuiltinHandlerForCustomJob() {
        QuartzJobHandlerRegistry registry = mock(QuartzJobHandlerRegistry.class);
        var request = new OpsJobCreateReq();
        request.setHandlerName("builtinHandler");
        when(registry.isBuiltin("builtinHandler")).thenReturn(true);
        var service =
                new OpsJobServiceImpl(
                        mock(QuartzJobManager.class),
                        registry,
                        mock(SysUserApi.class),
                        mock(OpsJobConverter.class),
                        mock(OpsJobLogService.class),
                        mock(OpsJobDashboardService.class));
        ReflectionTestUtils.setField(service, "baseMapper", mock(OpsJobMapper.class));

        assertThatThrownBy(() -> service.create(request))
                .hasMessageContaining("系统内置任务处理器不允许用于自定义任务");
    }

    @Test
    void shouldIncludeLatestExecutionWhenPaging() {
        OpsJobMapper mapper = mock(OpsJobMapper.class);
        SysUserApi userApi = mock(SysUserApi.class);
        OpsJobConverter converter = mock(OpsJobConverter.class);
        OpsJobLogService jobLogService = mock(OpsJobLogService.class);
        OpsJob job = job();
        job.setIsBuiltin(1);
        job.setCreateBy(1L);
        var response = new OpsJobPageResp();
        var service =
                new OpsJobServiceImpl(
                        mock(QuartzJobManager.class),
                        mock(QuartzJobHandlerRegistry.class),
                        userApi,
                        converter,
                        jobLogService,
                        mock(OpsJobDashboardService.class));
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        when(mapper.page(anyInt(), anyInt(), any(LambdaQueryWrapperX.class)))
                .thenReturn(new Page<OpsJob>().setRecords(List.of(job)).setTotal(1));
        when(converter.toPageResp(job)).thenReturn(response);
        when(userApi.getUsernameMapByIds(List.of(1L))).thenReturn(Map.of(1L, "admin"));
        var latestLog = new OpsJobLog();
        latestLog.setJobId(job.getId());
        latestLog.setStartTime(LocalDateTime.of(2026, 8, 16, 19, 0));
        latestLog.setStatus(1);
        when(jobLogService.latestByJobIds(List.of(job.getId())))
                .thenReturn(Map.of(job.getId(), latestLog));

        var result = service.page(new OpsJobPageReq());

        assertThat(result.getRecords()).containsExactly(response);
        assertThat(response.getLastExecutionTime()).isEqualTo(latestLog.getStartTime());
        assertThat(response.getLastExecutionStatus()).isEqualTo(1);
        assertThat(response.getCreateByUsername()).isEqualTo("admin");
    }

    private OpsJobServiceImpl service(QuartzJobManager manager, OpsJobMapper mapper) {
        QuartzJobHandlerRegistry registry = mock(QuartzJobHandlerRegistry.class);
        when(registry.contains("testHandler")).thenReturn(true);
        var service =
                new OpsJobServiceImpl(
                        manager,
                        registry,
                        mock(SysUserApi.class),
                        mock(OpsJobConverter.class),
                        mock(OpsJobLogService.class),
                        mock(OpsJobDashboardService.class));
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        return service;
    }

    private void initTransactionSynchronization() {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
    }

    private OpsJob job() {
        var job = new OpsJob();
        job.setId(1001L);
        job.setHandlerName("testHandler");
        job.setStatus(OpsJobStatus.DISABLED.getValue());
        job.setScheduleType("INTERVAL");
        job.setIntervalMillis(1000L);
        return job;
    }
}
