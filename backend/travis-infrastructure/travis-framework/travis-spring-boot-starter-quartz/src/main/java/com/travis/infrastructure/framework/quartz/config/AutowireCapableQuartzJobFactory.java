package com.travis.infrastructure.framework.quartz.config;

import lombok.AllArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

/** 使用 Spring 容器创建 Quartz Job，使统一 Job 可以注入业务扩展点。 */
@AllArgsConstructor
public class AutowireCapableQuartzJobFactory extends SpringBeanJobFactory {

    private final AutowireCapableBeanFactory beanFactory;

    @Override
    @NullMarked
    protected Object createJobInstance(TriggerFiredBundle bundle) {
        return beanFactory.createBean(bundle.getJobDetail().getJobClass());
    }
}
