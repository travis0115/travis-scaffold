package com.travis.infrastructure.framework.event.config;

import com.travis.infrastructure.framework.event.core.TransactionalApplicationEventPublisher;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

/** Spring 事务应用事件自动配置。 */
@AutoConfiguration(
        afterName = {
            "org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration",
            "org.springframework.boot.transaction.autoconfigure.TransactionAutoConfiguration"
        })
public class EventAutoConfiguration {

    /** 配置事务事件发布器。 */
    @Bean
    @ConditionalOnSingleCandidate(PlatformTransactionManager.class)
    @ConditionalOnMissingBean
    public TransactionalApplicationEventPublisher transactionalApplicationEventPublisher(
            ApplicationEventPublisher eventPublisher,
            PlatformTransactionManager transactionManager) {
        return new TransactionalApplicationEventPublisher(eventPublisher, transactionManager);
    }
}
