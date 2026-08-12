package com.travis.infrastructure.framework.monitor.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.travis.infrastructure.common.monitor.error.ErrorReporter;
import com.travis.infrastructure.framework.event.config.EventAutoConfiguration;
import com.travis.infrastructure.framework.event.core.TransactionalApplicationEventPublisher;
import com.travis.infrastructure.framework.monitor.core.CompositeErrorReporter;
import com.travis.infrastructure.framework.monitor.core.ErrorReporterContributor;
import com.travis.infrastructure.framework.monitor.core.ModulithErrorReporter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.task.SimpleAsyncTaskSchedulerCustomizer;
import org.springframework.boot.task.ThreadPoolTaskSchedulerCustomizer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.transaction.PlatformTransactionManager;

class MonitorAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(
                            AutoConfigurations.of(
                                    EventAutoConfiguration.class, MonitorAutoConfiguration.class));

    @Test
    void shouldBackOffCleanlyWithoutTransactionManager() {
        contextRunner.run(
                context -> {
                    assertThat(context)
                            .doesNotHaveBean(TransactionalApplicationEventPublisher.class);
                    assertThat(context).doesNotHaveBean(ErrorReporter.class);
                    assertThat(context).doesNotHaveBean(AsyncConfigurer.class);
                    assertThat(context).doesNotHaveBean(ThreadPoolTaskSchedulerCustomizer.class);
                    assertThat(context).doesNotHaveBean(SimpleAsyncTaskSchedulerCustomizer.class);
                });
    }

    @Test
    void shouldConfigureMonitoringWithTransactionManager() {
        contextRunner
                .withUserConfiguration(TransactionManagerConfiguration.class)
                .run(
                        context -> {
                            assertThat(context)
                                    .hasSingleBean(TransactionalApplicationEventPublisher.class);
                            assertThat(context).hasSingleBean(ErrorReporter.class);
                            assertThat(context).hasSingleBean(CompositeErrorReporter.class);
                            assertThat(context).hasSingleBean(ModulithErrorReporter.class);
                            assertThat(context).hasSingleBean(AsyncConfigurer.class);
                            assertThat(context)
                                    .hasSingleBean(ThreadPoolTaskSchedulerCustomizer.class);
                            assertThat(context)
                                    .hasSingleBean(SimpleAsyncTaskSchedulerCustomizer.class);
                        });
    }

    @Test
    void shouldKeepUserDefinedErrorReporter() {
        contextRunner
                .withUserConfiguration(
                        TransactionManagerConfiguration.class, CustomReporterConfiguration.class)
                .run(
                        context -> {
                            assertThat(context)
                                    .getBean(ErrorReporter.class)
                                    .isSameAs(
                                            context.getBean(CustomReporterConfiguration.class)
                                                    .errorReporter());
                            assertThat(context).doesNotHaveBean(CompositeErrorReporter.class);
                            assertThat(context).doesNotHaveBean(ModulithErrorReporter.class);
                        });
    }

    @Test
    void shouldCombineUserDefinedContributorWithModulithReporter() {
        contextRunner
                .withUserConfiguration(
                        TransactionManagerConfiguration.class, CustomContributorConfiguration.class)
                .run(
                        context -> {
                            assertThat(context).hasSingleBean(ErrorReporter.class);
                            assertThat(context).hasSingleBean(CompositeErrorReporter.class);
                            assertThat(context).getBeans(ErrorReporterContributor.class).hasSize(2);
                        });
    }

    @Configuration(proxyBeanMethods = false)
    static class TransactionManagerConfiguration {

        @Bean
        PlatformTransactionManager transactionManager() {
            return mock(PlatformTransactionManager.class);
        }
    }

    @Configuration(proxyBeanMethods = true)
    static class CustomReporterConfiguration {

        @Bean
        ErrorReporter errorReporter() {
            return event -> {};
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomContributorConfiguration {

        @Bean
        ErrorReporterContributor customContributor() {
            return event -> {};
        }
    }
}
