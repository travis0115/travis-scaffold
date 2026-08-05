package com.travis.monolith.system.message.internal.quartz;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** 负责在事务提交后同步消息一次性任务，并提供短周期失败重试。 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SysMessageScheduleCoordinator {
    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_DELAY_SECONDS = 10;

    private final ScheduledExecutorService retryExecutor =
            Executors.newSingleThreadScheduledExecutor(
                    Thread.ofPlatform()
                            .name("system-message-schedule-sync-retry")
                            .daemon(true)
                            .factory());

    private final SysMessageScheduledPushScheduler scheduler;

    /** 事务提交后根据消息最新数据库状态同步一次性任务。 */
    public void syncAfterCommit(Long messageId) {
        Runnable action = () -> runSync(messageId, 1);
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            action.run();
                        }
                    });
            return;
        }
        action.run();
    }

    private void runSync(Long messageId, int attempt) {
        try {
            scheduler.sync(messageId);
        } catch (Exception exception) {
            if (attempt >= MAX_ATTEMPTS) {
                log.error(
                        "[消息调度] 同步一次性任务失败，messageId={}, attempts={}",
                        messageId,
                        attempt,
                        exception);
                return;
            }
            log.warn(
                    "[消息调度] 同步一次性任务失败，准备重试，messageId={}, attempt={}",
                    messageId,
                    attempt,
                    exception);
            try {
                retryExecutor.schedule(
                        () -> runSync(messageId, attempt + 1),
                        RETRY_DELAY_SECONDS,
                        TimeUnit.SECONDS);
            } catch (RuntimeException retryException) {
                log.error(
                        "[消息调度] 提交一次性任务重试失败，messageId={}, attempt={}",
                        messageId,
                        attempt,
                        retryException);
            }
        }
    }

    @PreDestroy
    void shutdownRetryExecutor() {
        retryExecutor.shutdownNow();
    }
}
