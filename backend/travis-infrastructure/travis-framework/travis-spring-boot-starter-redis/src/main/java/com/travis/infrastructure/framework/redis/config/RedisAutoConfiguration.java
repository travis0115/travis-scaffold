package com.travis.infrastructure.framework.redis.config;

import cn.hutool.core.text.CharSequenceUtil;
import com.travis.infrastructure.framework.jackson.config.JacksonAutoConfiguration;
import com.travis.infrastructure.framework.jackson.core.LaissezFaireSubTypeValidator;
import com.travis.infrastructure.framework.redis.config.properties.TravisRedisProperties;
import com.travis.infrastructure.framework.redis.core.key.RedisKeyPrefixResolver;
import com.travis.infrastructure.framework.redis.core.pubsub.RedisPubSubClient;
import com.travis.infrastructure.framework.redis.core.serializer.TravisJacksonJsonRedisSerializer;
import com.travis.infrastructure.framework.redis.core.task.ClusterPeriodicTaskExecutor;
import com.travis.infrastructure.framework.redis.core.util.RedisUtil;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jackson.autoconfigure.JacksonProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.RedisSerializer;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

/**
 * Redis 配置类
 *
 * @author travis
 */
@AllArgsConstructor
@AutoConfiguration(after = JacksonAutoConfiguration.class)
@EnableConfigurationProperties(TravisRedisProperties.class)
public class RedisAutoConfiguration {

    private final JacksonProperties jacksonProperties;

    @Bean
    @ConditionalOnMissingBean
    public RedisKeyPrefixResolver redisKeyPrefixResolver(TravisRedisProperties properties) {
        return new RedisKeyPrefixResolver(properties);
    }

    /** 创建 RedisTemplate Bean，使用 JSON 序列化方式 */
    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory redisConnectionFactory) {
        // 创建 RedisTemplate 对象
        var template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(redisConnectionFactory);

        var stringRedisSerializer = RedisSerializer.string();

        var jsonMapper = createRedisObjectMapper();
        var jsonRedisSerializer = new TravisJacksonJsonRedisSerializer(jsonMapper);

        // 使用 String 序列化 KEY
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);

        // 使用 JSON 序列化 VALUE
        template.setValueSerializer(jsonRedisSerializer);
        template.setHashValueSerializer(jsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }

    private ObjectMapper createRedisObjectMapper() {
        return JsonMapper.builder()
                .addModule(redisJavaTimeModule())
                .disable(
                        DeserializationFeature.FAIL_ON_TRAILING_TOKENS) // 成功解析值后不验证是否存在额外内容(缓存内容可信)
                // 不可全局设置，会导致Spring Boot Actuator等Object/JsonMapper污染
                // 添加默认类型到属性中，LaissezFaireSubTypeValidator
                // 是全局放行的校验器，相当于关闭了校验，仅在序列化对象可信时使用，否则请使用白名单校验器
                // BasicPolymorphicTypeValidator
                .activateDefaultTypingAsProperty(
                        new LaissezFaireSubTypeValidator(),
                        DefaultTyping.NON_FINAL_AND_ENUMS,
                        "@class")
                .build();
    }

    private JacksonModule redisJavaTimeModule() {
        var javaTimeModule = new SimpleModule();
        if (CharSequenceUtil.isNotBlank(jacksonProperties.getDateFormat())) {
            var formatter = DateTimeFormatter.ofPattern(jacksonProperties.getDateFormat());
            javaTimeModule
                    .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter))
                    .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
        }
        return javaTimeModule;
    }

    /** 创建 RedisUtils Bean，注入redisTemplate */
    @Bean
    public RedisUtil redisUtil(
            RedisTemplate<String, Object> redisTemplate,
            ObjectProvider<CacheKeyPrefix> cacheKeyPrefixProvider,
            RedisKeyPrefixResolver redisKeyPrefixResolver) {
        var util = new RedisUtil();
        util.setRedisTemplate(redisTemplate);
        util.setCacheKeyPrefixProvider(cacheKeyPrefixProvider);
        util.setRedisKeyPrefixResolver(redisKeyPrefixResolver);
        return util;
    }

    /** 创建 Redis Pub/Sub 消息监听容器 */
    @Bean
    @ConditionalOnMissingBean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory) {
        var container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        return container;
    }

    /** 创建 Redis Pub/Sub 客户端 */
    @Bean
    @ConditionalOnMissingBean
    public RedisPubSubClient redisPubSubClient(
            RedisTemplate<String, Object> redisTemplate,
            RedisMessageListenerContainer listenerContainer,
            RedisKeyPrefixResolver redisKeyPrefixResolver) {
        return new RedisPubSubClient(redisTemplate, listenerContainer, redisKeyPrefixResolver);
    }

    /** 集群周期任务执行器。 */
    @Bean
    @ConditionalOnMissingBean
    public ClusterPeriodicTaskExecutor clusterPeriodicTaskExecutor(
            RedissonClient redissonClient,
            RedisTemplate<String, Object> redisTemplate,
            RedisKeyPrefixResolver redisKeyPrefixResolver) {
        return new ClusterPeriodicTaskExecutor(
                redissonClient, redisTemplate, redisKeyPrefixResolver);
    }
}
