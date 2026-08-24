package com.travis.infrastructure.framework.quartz.config;

import com.travis.infrastructure.common.monitor.error.ErrorReporter;
import com.travis.infrastructure.framework.quartz.core.*;
import org.quartz.Scheduler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.quartz.autoconfigure.SchedulerFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;

/** Quartz 任务处理器与 Spring JobFactory 自动配置。 */
@AutoConfiguration
public class QuartzAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public QuartzJobHandlerRegistry quartzJobHandlerRegistry(
            ObjectProvider<QuartzJobHandler> handlers) {
        return new QuartzJobHandlerRegistry(handlers);
    }

    @Bean
    @ConditionalOnMissingBean
    public QuartzJobExecutionObserver quartzJobExecutionObserver() {
        return new QuartzJobExecutionObserver() {};
    }

    @Bean
    @ConditionalOnMissingBean
    public QuartzOneShotManager quartzOneShotManager(Scheduler scheduler) {
        return new QuartzOneShotManager(scheduler);
    }

    @Bean
    @ConditionalOnMissingBean
    public QuartzSyncExecutor quartzSyncExecutor() {
        return new QuartzSyncExecutor();
    }

    @Bean
    @ConditionalOnMissingBean
    public ErrorQuartzJobListener errorQuartzJobListener(ErrorReporter errorReporter) {
        return new ErrorQuartzJobListener(errorReporter);
    }

    @Bean
    public SchedulerFactoryBeanCustomizer travisQuartzJobFactoryCustomizer(
            AutowireCapableBeanFactory beanFactory, ErrorQuartzJobListener systemErrorJobListener) {
        return schedulerFactoryBean -> {
            schedulerFactoryBean.setJobFactory(new AutowireCapableQuartzJobFactory(beanFactory));
            schedulerFactoryBean.setGlobalJobListeners(systemErrorJobListener);
        };
    }
}
