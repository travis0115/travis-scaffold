package com.travis.monolith.system.message.internal.quartz;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.travis.infrastructure.common.monitor.error.ErrorReporter;
import com.travis.infrastructure.framework.redis.core.task.ClusterPeriodicTaskExecutor;
import org.junit.jupiter.api.Test;

class SysMessageScheduledPushReconcileTaskTest {

    @Test
    void shouldExposeReconcileAsOpsJobHandler() {
        SysMessageScheduledPushScheduler scheduler = mock(SysMessageScheduledPushScheduler.class);
        var task =
                new SysMessageScheduledPushReconcileTask(
                        scheduler,
                        mock(ClusterPeriodicTaskExecutor.class),
                        mock(ErrorReporter.class));

        task.execute("{}");

        verify(scheduler).reconcile();
    }
}
