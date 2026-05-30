package com.travis.infrastructure.framework.desensitize.config;

import com.travis.infrastructure.framework.desensitize.core.jackson.modules.DesensitizeJacksonModule;
import com.travis.infrastructure.framework.desensitize.core.spi.DefaultDesensitizeObjectSerializer;
import com.travis.infrastructure.framework.desensitize.core.spel.DefaultEvaluationContextProvider;
import com.travis.infrastructure.framework.desensitize.core.spel.EvaluationContextProvider;
import com.travis.infrastructure.framework.desensitize.core.spi.DesensitizeObjectSerializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.ObjectMapper;

@AutoConfiguration
public class TravisDesensitizeAutoConfiguration {

    /**
     * 脱敏 Module， @DesensitizeBy 绑定序列化器
     */
    @Bean
    public JacksonModule desensitizeJacksonModule() {
        return new DesensitizeJacksonModule();
    }

    /**
     * 注册 SPI 实现脱敏工具类，提供给 DesensitizeUtils
     */
    @Bean
    @ConditionalOnMissingBean
    public DesensitizeObjectSerializer desensitizeObjectSerializer(ObjectMapper objectMapper) {
        return new DefaultDesensitizeObjectSerializer(objectMapper);
    }

    /**
     * 默认的SpEL表达式解析器上下文
     * 用于脱敏注解 disable 表达式，可从上下文中获取 MDC、Spring Bean 等自定义内容
     */
    @Bean
    @ConditionalOnMissingBean
    public EvaluationContextProvider evaluationContextProvider() {
        return new DefaultEvaluationContextProvider();
    }

}