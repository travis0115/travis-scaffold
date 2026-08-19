package com.travis.monolith.system.message.internal.quartz;

import com.travis.infrastructure.common.monitor.error.ErrorReporter;
import com.travis.infrastructure.common.monitor.error.ErrorSource;
import com.travis.infrastructure.framework.quartz.core.QuartzJobHandler;
import com.travis.infrastructure.framework.redis.core.task.ClusterPeriodicTaskExecutor;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** 每五分钟对账消息定时任务。 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SysMessageScheduledPushReconcileTask implements QuartzJobHandler {
    private static final String HANDLER_NAME = "sysMessageScheduledPushReconcile";
    private static final long RECONCILE_INTERVAL_MILLIS = 300_000;
    private static final Duration RECONCILE_INTERVAL = Duration.ofMillis(RECONCILE_INTERVAL_MILLIS);

    private final SysMessageScheduledPushScheduler scheduler;
    private final ClusterPeriodicTaskExecutor periodicTaskExecutor;
    private final ErrorReporter errorReporter;

    /** 应用启动时执行全局限频对账。 */
    public void run() {
        try {
            periodicTaskExecutor.executeOncePerInterval(
                    SysMessageScheduledPushNames.LOCK_NAMESPACE,
                    SysMessageScheduledPushNames.RECONCILE_LOCK_KEY,
                    RECONCILE_INTERVAL,
                    scheduler::reconcile);
        } catch (Exception exception) {
            log.error("[消息调度] 对账补齐一次性任务失败", exception);
            errorReporter.report(
                    ErrorSource.SCHEDULING,
                    getClass().getName() + "#run",
                    SysMessageScheduledPushNames.RECONCILE_LOCK_KEY,
                    exception);
        }
    }

    @Override
    public String getName() {
        return HANDLER_NAME;
    }

    @Override
    public String getDescription() {
        return "消息定时推送任务对账";
    }

    @Override
    public boolean isBuiltin() {
        return true;
    }

    /** 由运维调度中心执行消息任务对账，异常交由统一执行日志记录。 */
    @Override
    public void execute(String params) {
        scheduler.reconcile();
    }
}
