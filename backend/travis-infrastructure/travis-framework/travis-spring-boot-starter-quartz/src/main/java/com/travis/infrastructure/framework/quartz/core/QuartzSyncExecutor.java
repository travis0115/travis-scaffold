package com.travis.infrastructure.framework.quartz.core;

import com.travis.infrastructure.common.transaction.AfterCommitExecutor;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/** 在事务提交后执行 Quartz 同步，并对短暂失败进行有限重试。 */
@Slf4j
public class QuartzSyncExecutor {

    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_DELAY_SECONDS = 10;

    private final ScheduledExecutorService retryExecutor =
            Executors.newSingleThreadScheduledExecutor(
                    Thread.ofPlatform().name("quartz-sync-retry").daemon(true).factory());

    public void executeAfterCommit(String taskDescription, Runnable action) {
        AfterCommitExecutor.execute(() -> run(taskDescription, action, 1));
    }

    private void run(String taskDescription, Runnable action, int attempt) {
        try {
            action.run();
        } catch (Exception exception) {
            if (attempt >= MAX_ATTEMPTS) {
                log.error("Quartz 同步失败，task={}, attempts={}", taskDescription, attempt, exception);
                return;
            }
            log.warn("Quartz 同步失败，准备重试，task={}, attempt={}", taskDescription, attempt, exception);
            try {
                retryExecutor.schedule(
                        () -> run(taskDescription, action, attempt + 1),
                        RETRY_DELAY_SECONDS,
                        TimeUnit.SECONDS);
            } catch (RuntimeException retryException) {
                log.error(
                        "提交 Quartz 同步重试失败，task={}, attempt={}",
                        taskDescription,
                        attempt,
                        retryException);
            }
        }
    }

    @PreDestroy
    void shutdown() {
        retryExecutor.shutdownNow();
    }
}
