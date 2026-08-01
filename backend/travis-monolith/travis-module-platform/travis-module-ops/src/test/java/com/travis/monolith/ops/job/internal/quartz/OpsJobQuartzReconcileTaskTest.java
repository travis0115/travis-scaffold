package com.travis.monolith.ops.job.internal.quartz;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.travis.infrastructure.framework.redis.core.util.RedisUtil;
import com.travis.monolith.ops.job.internal.service.OpsJobLogService;
import com.travis.monolith.ops.job.internal.service.OpsJobService;
import com.travis.monolith.ops.job.internal.service.QuartzJobManager;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class OpsJobQuartzReconcileTaskTest {

    @Test
    void shouldReconcileJobsAndInterruptedLogsAfterClaimingSlot() {
        OpsJobService jobService = mock(OpsJobService.class);
        OpsJobLogService jobLogService = mock(OpsJobLogService.class);
        QuartzJobManager quartzJobManager = mock(QuartzJobManager.class);
        var task = new OpsJobQuartzReconcileTask(jobService, jobLogService, quartzJobManager);
        when(jobService.listAll()).thenReturn(List.of());
        try (MockedStatic<RedisUtil> redisUtil = Mockito.mockStatic(RedisUtil.class)) {
            redisUtil
                    .when(
                            () ->
                                    RedisUtil.setIfAbsent(
                                            any(String.class), eq(Boolean.TRUE), anyLong()))
                    .thenReturn(true);

            task.run();

            verify(quartzJobManager).reconcile(List.of());
            verify(jobLogService).markInterruptedExecutions();
        }
    }
}
