package com.travis.infrastructure.framework.event.core;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/** 按指定事务传播语义发布 Spring 应用事件，便于事务事件监听器按提交时机触发。 */
public class TransactionalApplicationEventPublisher {

    private final ApplicationEventPublisher eventPublisher;
    private final TransactionTemplate transactionTemplate;
    private final TransactionTemplate requiresNewTransactionTemplate;

    public TransactionalApplicationEventPublisher(
            ApplicationEventPublisher eventPublisher,
            PlatformTransactionManager transactionManager) {
        this.eventPublisher = eventPublisher;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.requiresNewTransactionTemplate = new TransactionTemplate(transactionManager);
        this.requiresNewTransactionTemplate.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /** 加入当前事务发布事件；无事务时新建事务。 */
    public void publishEvent(Object event) {
        transactionTemplate.executeWithoutResult(status -> eventPublisher.publishEvent(event));
    }

    /** 挂起当前事务并在独立新事务中发布事件。 */
    public void publishEventRequiresNew(Object event) {
        requiresNewTransactionTemplate.executeWithoutResult(
                status -> eventPublisher.publishEvent(event));
    }
}
