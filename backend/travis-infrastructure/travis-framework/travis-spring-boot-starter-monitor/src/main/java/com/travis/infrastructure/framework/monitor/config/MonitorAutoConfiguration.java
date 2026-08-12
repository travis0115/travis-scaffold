package com.travis.infrastructure.framework.monitor.config;

import com.travis.infrastructure.common.monitor.error.ErrorReporter;
import com.travis.infrastructure.common.monitor.error.ErrorSource;
import com.travis.infrastructure.framework.event.config.EventAutoConfiguration;
import com.travis.infrastructure.framework.event.core.TransactionalApplicationEventPublisher;
import com.travis.infrastructure.framework.monitor.core.CompositeErrorReporter;
import com.travis.infrastructure.framework.monitor.core.ErrorAsyncConfigurer;
import com.travis.infrastructure.framework.monitor.core.ErrorReporterContributor;
import com.travis.infrastructure.framework.monitor.core.ModulithErrorReporter;
import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.task.SimpleAsyncTaskSchedulerCustomizer;
import org.springframework.boot.task.ThreadPoolTaskSchedulerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.AsyncConfigurer;

/** 系统异常采集与上报自动配置。 */
@AutoConfiguration(
        after = EventAutoConfiguration.class,
        beforeName = {
            "org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration",
            "org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration"
        })
public class MonitorAutoConfiguration {

    /** 配置基于 Spring Modulith 持久化事件的系统异常上报器。 */
    @Bean
    @ConditionalOnBean(TransactionalApplicationEventPublisher.class)
    @ConditionalOnMissingBean({ErrorReporter.class, ModulithErrorReporter.class})
    public ModulithErrorReporter modulithErrorReporter(
            TransactionalApplicationEventPublisher eventPublisher) {
        return new ModulithErrorReporter(eventPublisher);
    }

    /** 将全部异常上报渠道组合为统一上报端口。 */
    @Bean
    @Primary
    @ConditionalOnBean(ErrorReporterContributor.class)
    @ConditionalOnMissingBean(ErrorReporter.class)
    public ErrorReporter errorReporter(List<ErrorReporterContributor> contributors) {
        return new CompositeErrorReporter(contributors);
    }

    /** 配置无返回值异步方法的兜底异常上报。 */
    @Bean
    @ConditionalOnBean(ErrorReporter.class)
    @ConditionalOnMissingBean(AsyncConfigurer.class)
    public AsyncConfigurer errorAsyncConfigurer(ErrorReporter errorReporter) {
        return new ErrorAsyncConfigurer(errorReporter);
    }

    /** 配置平台线程调度器的兜底异常上报。 */
    @Bean
    @ConditionalOnBean(ErrorReporter.class)
    public ThreadPoolTaskSchedulerCustomizer errorThreadPoolTaskSchedulerCustomizer(
            ErrorReporter errorReporter) {
        return scheduler ->
                scheduler.setErrorHandler(
                        throwable ->
                                errorReporter.report(
                                        ErrorSource.SCHEDULING,
                                        Thread.currentThread().getName(),
                                        null,
                                        throwable));
    }

    /** 配置虚拟线程调度器的兜底异常上报。 */
    @Bean
    @ConditionalOnBean(ErrorReporter.class)
    public SimpleAsyncTaskSchedulerCustomizer errorSimpleTaskSchedulerCustomizer(
            ErrorReporter errorReporter) {
        return scheduler ->
                scheduler.setErrorHandler(
                        throwable ->
                                errorReporter.report(
                                        ErrorSource.SCHEDULING,
                                        Thread.currentThread().getName(),
                                        null,
                                        throwable));
    }
}
