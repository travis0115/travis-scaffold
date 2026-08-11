package com.travis.monolith.ops.job.internal.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.travis.infrastructure.framework.quartz.core.QuartzJobHandlerRegistry;
import com.travis.monolith.ops.job.api.enums.OpsJobStatus;
import com.travis.monolith.ops.job.internal.converter.OpsJobConverter;
import com.travis.monolith.ops.job.internal.entity.OpsJob;
import com.travis.monolith.ops.job.internal.mapper.OpsJobMapper;
import com.travis.monolith.ops.job.internal.service.impl.OpsJobServiceImpl;
import com.travis.monolith.system.user.api.SysUserApi;
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

    private OpsJobServiceImpl service(QuartzJobManager manager, OpsJobMapper mapper) {
        QuartzJobHandlerRegistry registry = mock(QuartzJobHandlerRegistry.class);
        when(registry.contains("testHandler")).thenReturn(true);
        var service =
                new OpsJobServiceImpl(
                        manager, registry, mock(SysUserApi.class), mock(OpsJobConverter.class));
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
        return job;
    }
}
