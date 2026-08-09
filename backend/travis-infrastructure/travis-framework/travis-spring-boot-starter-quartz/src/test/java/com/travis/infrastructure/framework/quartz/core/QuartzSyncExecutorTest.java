package com.travis.infrastructure.framework.quartz.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class QuartzSyncExecutorTest {

    private final QuartzSyncExecutor executor = new QuartzSyncExecutor();

    @AfterEach
    void tearDown() {
        executor.shutdown();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Test
    void shouldExecuteImmediatelyWithoutTransaction() {
        var count = new AtomicInteger();

        executor.executeAfterCommit("test", count::incrementAndGet);

        assertThat(count).hasValue(1);
    }

    @Test
    void shouldExecuteOnlyAfterTransactionCommit() {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
        var count = new AtomicInteger();

        executor.executeAfterCommit("test", count::incrementAndGet);

        assertThat(count).hasValue(0);
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(TransactionSynchronization::afterCommit);
        assertThat(count).hasValue(1);
    }
}
