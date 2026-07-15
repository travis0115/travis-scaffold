package com.travis.infrastructure.framework.web.core.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionTemplate;

/** 在事务中发布 Spring 应用事件，便于事务事件监听器按提交时机触发。 */
@RequiredArgsConstructor
public class TransactionalApplicationEventPublisher {

    private final ApplicationEventPublisher eventPublisher;
    private final TransactionTemplate transactionTemplate;

    /** 在事务提交后发布事件；无事务时立即发布。 */
    public void publishEvent(Object event) {
        transactionTemplate.executeWithoutResult(status -> eventPublisher.publishEvent(event));
    }
}
