package com.travis.monolith.ops.job.internal.quartz;

import com.travis.infrastructure.common.monitor.error.ErrorReporter;
import com.travis.infrastructure.common.monitor.error.ErrorSource;
import com.travis.infrastructure.framework.redis.core.task.ClusterPeriodicTaskExecutor;
import com.travis.monolith.ops.job.internal.service.OpsJobLogService;
import com.travis.monolith.ops.job.internal.service.OpsJobService;
import com.travis.monolith.ops.job.internal.service.QuartzJobManager;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 定期将任务业务配置与 Quartz 持久化状态对账。 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OpsJobQuartzReconcileTask {

    private static final long RECONCILE_INTERVAL_MILLIS = 60_000;
    private static final Duration RECONCILE_INTERVAL = Duration.ofMillis(RECONCILE_INTERVAL_MILLIS);

    private final OpsJobService jobService;
    private final OpsJobLogService jobLogService;
    private final QuartzJobManager quartzJobManager;
    private final ClusterPeriodicTaskExecutor periodicTaskExecutor;
    private final ErrorReporter errorReporter;

    /** 在集群内按分钟限频执行对账。 */
    @Scheduled(initialDelay = RECONCILE_INTERVAL_MILLIS, fixedDelay = RECONCILE_INTERVAL_MILLIS)
    public void run() {
        try {
            periodicTaskExecutor.executeOncePerInterval(
                    QuartzJobManager.LOCK_NAMESPACE,
                    QuartzJobManager.RECONCILE_LOCK_KEY,
                    RECONCILE_INTERVAL,
                    () -> {
                        quartzJobManager.reconcile(jobService.listAll());
                        jobLogService.markInterruptedExecutions();
                    });
        } catch (Exception exception) {
            log.error("Quartz 任务配置对账失败", exception);
            errorReporter.report(
                    ErrorSource.SCHEDULING,
                    getClass().getName() + "#run",
                    QuartzJobManager.RECONCILE_LOCK_KEY,
                    exception);
        }
    }
}
