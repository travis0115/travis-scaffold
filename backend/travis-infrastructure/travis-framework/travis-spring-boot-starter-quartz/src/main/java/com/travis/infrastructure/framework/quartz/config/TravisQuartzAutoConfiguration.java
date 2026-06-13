package com.travis.infrastructure.framework.quartz.config;

import com.travis.infrastructure.framework.quartz.core.QuartzJobExecutionObserver;
import com.travis.infrastructure.framework.quartz.core.QuartzJobHandler;
import com.travis.infrastructure.framework.quartz.core.QuartzJobHandlerRegistry;
import java.util.List;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.quartz.autoconfigure.SchedulerFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;

/** Quartz 任务处理器与 Spring JobFactory 自动配置。 */
@AutoConfiguration
public class TravisQuartzAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public QuartzJobHandlerRegistry quartzJobHandlerRegistry(List<QuartzJobHandler> handlers) {
        return new QuartzJobHandlerRegistry(handlers);
    }

    @Bean
    @ConditionalOnMissingBean
    public QuartzJobExecutionObserver quartzJobExecutionObserver() {
        return new QuartzJobExecutionObserver() {};
    }

    @Bean
    public SchedulerFactoryBeanCustomizer travisQuartzJobFactoryCustomizer(
            AutowireCapableBeanFactory beanFactory) {
        return schedulerFactoryBean ->
                schedulerFactoryBean.setJobFactory(
                        new AutowireCapableQuartzJobFactory(beanFactory));
    }
}
