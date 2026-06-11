package com.travis.monolith.system.log.operationlog.internal.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/** 操作日志异步执行器配置。 */
@Configuration(proxyBeanMethods = false)
@EnableAsync
public class OperationLogAsyncConfiguration {

    @Bean
    public Executor operationLogTaskExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("operation-log-");
        executor.initialize();
        return executor;
    }
}
