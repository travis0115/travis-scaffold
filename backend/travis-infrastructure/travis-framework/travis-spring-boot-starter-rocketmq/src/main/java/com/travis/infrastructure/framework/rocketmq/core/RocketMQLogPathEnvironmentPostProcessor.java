package com.travis.infrastructure.framework.rocketmq.core;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 在 Spring 环境准备阶段设置 RocketMQ 客户端日志路径系统属性。
 *
 * <p>rocketmq-client-java（v5 gRPC 客户端）内置 shaded logback， 在类加载阶段就会读取 {@code rocketmq.log.root}
 * 系统属性初始化日志。 普通的 Spring Bean 或 AutoConfiguration 执行时已经太晚， 必须通过 {@link EnvironmentPostProcessor}
 * 在环境准备阶段就设置好。
 *
 * <p>优先级：环境变量 {@code ROCKETMQ_LOG_ROOT} > 配置项 {@code travis.rocketmq.client.log-path} > 默认值
 *
 * @author travis
 */
public class RocketMQLogPathEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_KEY = "travis.rocketmq.client.log-path";
    private static final String ENV_OVERRIDE = "ROCKETMQ_LOG_ROOT";
    private static final String SYSTEM_PROPERTY = "rocketmq.log.root";
    private static final String DEFAULT_PATH = "${user.home}/data/logs/rocketmq";

    @Override
    public void postProcessEnvironment(
            @NonNull ConfigurableEnvironment environment, @NonNull SpringApplication application) {
        // 环境变量优先级最高
        String logPath = System.getenv(ENV_OVERRIDE);
        if (logPath == null || logPath.isBlank()) {
            // 读取 application.yml 中的配置，未配置则使用默认值
            logPath = environment.getProperty(PROPERTY_KEY, DEFAULT_PATH);
        }
        // 解析 ${user.home} 等占位符
        logPath = environment.resolveRequiredPlaceholders(logPath);
        System.setProperty(SYSTEM_PROPERTY, logPath);
    }
}
