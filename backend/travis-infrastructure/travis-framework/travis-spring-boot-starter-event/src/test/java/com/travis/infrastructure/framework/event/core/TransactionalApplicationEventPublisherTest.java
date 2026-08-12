package com.travis.infrastructure.framework.event.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

class TransactionalApplicationEventPublisherTest {

    @Test
    void shouldJoinCurrentTransactionByDefault() {
        var eventPublisher = mock(ApplicationEventPublisher.class);
        var transactionManager = mock(PlatformTransactionManager.class);
        var transactionStatus = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        var publisher =
                new TransactionalApplicationEventPublisher(eventPublisher, transactionManager);
        var event = new Object();

        publisher.publishEvent(event);

        assertPropagation(transactionManager, TransactionDefinition.PROPAGATION_REQUIRED);
        verify(eventPublisher).publishEvent(event);
        verify(transactionManager).commit(transactionStatus);
    }

    @Test
    void shouldPublishInRequiresNewTransaction() {
        var eventPublisher = mock(ApplicationEventPublisher.class);
        var transactionManager = mock(PlatformTransactionManager.class);
        var transactionStatus = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        var publisher =
                new TransactionalApplicationEventPublisher(eventPublisher, transactionManager);
        var event = new Object();

        publisher.publishEventRequiresNew(event);

        assertPropagation(transactionManager, TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        verify(eventPublisher).publishEvent(event);
        verify(transactionManager).commit(transactionStatus);
    }

    private void assertPropagation(
            PlatformTransactionManager transactionManager, int expectedPropagation) {
        var definition = ArgumentCaptor.forClass(TransactionDefinition.class);
        verify(transactionManager).getTransaction(definition.capture());
        assertThat(definition.getValue().getPropagationBehavior()).isEqualTo(expectedPropagation);
    }
}
