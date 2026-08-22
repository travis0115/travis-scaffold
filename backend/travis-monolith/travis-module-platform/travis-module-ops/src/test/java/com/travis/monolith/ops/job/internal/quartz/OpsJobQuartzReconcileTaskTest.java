package com.travis.monolith.ops.job.internal.quartz;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.travis.infrastructure.common.monitor.error.ErrorReporter;
import com.travis.infrastructure.framework.redis.core.task.ClusterPeriodicTaskExecutor;
import com.travis.monolith.ops.job.internal.service.OpsJobLogService;
import com.travis.monolith.ops.job.internal.service.OpsJobService;
import com.travis.monolith.ops.job.internal.service.QuartzJobManager;
import java.util.List;
import org.junit.jupiter.api.Test;

class OpsJobQuartzReconcileTaskTest {

    @Test
    void shouldReconcileJobsAndInterruptedLogsAfterClaimingSlot() {
        OpsJobService jobService = mock(OpsJobService.class);
        OpsJobLogService jobLogService = mock(OpsJobLogService.class);
        QuartzJobManager quartzJobManager = mock(QuartzJobManager.class);
        ClusterPeriodicTaskExecutor periodicTaskExecutor = mock(ClusterPeriodicTaskExecutor.class);
        var task =
                new OpsJobQuartzReconcileTask(
                        jobService,
                        jobLogService,
                        quartzJobManager,
                        periodicTaskExecutor,
                        mock(ErrorReporter.class));
        when(jobService.list()).thenReturn(List.of());
        doAnswer(
                        invocation -> {
                            invocation.<Runnable>getArgument(3).run();
                            return true;
                        })
                .when(periodicTaskExecutor)
                .executeOncePerInterval(any(), any(), any(), any());

        task.run();

        verify(quartzJobManager).reconcile(List.of());
        verify(jobLogService).markInterruptedExecutions();
    }

    @Test
    void shouldExposeReconcileAsOpsJobHandler() {
        OpsJobService jobService = mock(OpsJobService.class);
        OpsJobLogService jobLogService = mock(OpsJobLogService.class);
        QuartzJobManager quartzJobManager = mock(QuartzJobManager.class);
        var task =
                new OpsJobQuartzReconcileTask(
                        jobService,
                        jobLogService,
                        quartzJobManager,
                        mock(ClusterPeriodicTaskExecutor.class),
                        mock(ErrorReporter.class));
        when(jobService.list()).thenReturn(List.of());

        task.execute("{}");

        verify(quartzJobManager).reconcile(List.of());
        verify(jobLogService).markInterruptedExecutions();
    }
}
