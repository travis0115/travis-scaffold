package com.travis.monolith.ops.job.internal.quartz;

import com.travis.infrastructure.common.monitor.error.ErrorReporter;
import com.travis.infrastructure.common.monitor.error.ErrorSource;
import com.travis.infrastructure.framework.quartz.core.QuartzJobHandler;
import com.travis.infrastructure.framework.redis.core.task.ClusterPeriodicTaskExecutor;
import com.travis.monolith.ops.job.internal.service.OpsJobLogService;
import com.travis.monolith.ops.job.internal.service.OpsJobService;
import com.travis.monolith.ops.job.internal.service.QuartzJobManager;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** 定期将任务业务配置与 Quartz 持久化状态对账。 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OpsJobQuartzReconcileTask implements QuartzJobHandler {

    private static final String HANDLER_NAME = "opsJobQuartzReconcile";

    private static final long RECONCILE_INTERVAL_MILLIS = 60_000;
    private static final Duration RECONCILE_INTERVAL = Duration.ofMillis(RECONCILE_INTERVAL_MILLIS);

    private final OpsJobService jobService;
    private final OpsJobLogService jobLogService;
    private final QuartzJobManager quartzJobManager;
    private final ClusterPeriodicTaskExecutor periodicTaskExecutor;
    private final ErrorReporter errorReporter;

    /** 应用启动时在集群内限频执行对账。 */
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

    @Override
    public String getName() {
        return HANDLER_NAME;
    }

    @Override
    public String getDescription() {
        return "任务配置与 Quartz 状态对账";
    }

    @Override
    public boolean isBuiltin() {
        return true;
    }

    /** 由运维调度中心执行对账，异常交由统一执行日志记录。 */
    @Override
    public void execute(String params) {
        reconcile();
    }

    private void reconcile() {
        quartzJobManager.reconcile(jobService.listAll());
        jobLogService.markInterruptedExecutions();
    }
}
