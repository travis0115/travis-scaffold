package com.travis.infrastructure.framework.redis.config;

import cn.hutool.core.util.StrUtil;
import com.travis.infrastructure.framework.redis.config.properties.TravisRedisProperties;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.cache.autoconfigure.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;

/**
 * Cache 配置类，基于Redis
 *
 * @author travis
 */
@AutoConfiguration(after = RedisAutoConfiguration.class)
@EnableCaching
@EnableConfigurationProperties({CacheProperties.class})
public class CacheAutoConfiguration {

    /** 缓存键前缀 */
    @Bean
    public CacheKeyPrefix cacheKeyPrefix(CacheProperties cacheProperties) {
        var redisProperties = cacheProperties.getRedis();
        return cacheName -> {
            var keyPrefix = redisProperties.getKeyPrefix();
            if (!redisProperties.isUseKeyPrefix()) {
                keyPrefix = null;
            }
            if (StrUtil.isNotBlank(keyPrefix)) {
                keyPrefix =
                        keyPrefix.lastIndexOf(StrUtil.COLON) == -1
                                ? keyPrefix + StrUtil.COLON
                                : keyPrefix;
                return keyPrefix + cacheName + StrUtil.COLON;
            }
            return cacheName + StrUtil.COLON;
        };
    }

    /** 基于Redis的缓存配置 */
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(
            CacheProperties cacheProperties,
            CacheKeyPrefix cacheKeyPrefix,
            RedisTemplate<String, Object> redisTemplate) {
        var config = RedisCacheConfiguration.defaultCacheConfig();
        var redisProperties = cacheProperties.getRedis();
        // 设置默认缓存时间
        if (redisProperties.getTimeToLive() != null) {
            config = config.entryTtl(redisProperties.getTimeToLive());
        }
        // 不缓存null
        if (!redisProperties.isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }
        // 自定义缓存前缀名
        config = config.computePrefixWith(cacheKeyPrefix);
        // 设置使用 JSON 序列化方式
        config =
                config.serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                redisTemplate.getValueSerializer()));
        return config;
    }

    /** 配置缓存管理器 */
    @Bean
    public CacheManager cacheManager(
            RedisCacheConfiguration redisCacheConfiguration,
            RedisTemplate<String, Object> redisTemplate,
            TravisRedisProperties travisRedisProperties) {
        Map<String, RedisCacheConfiguration> initialConfigurations = new LinkedHashMap<>();
        travisRedisProperties
                .getCacheTtl()
                .forEach(
                        (cacheName, ttl) ->
                                initialConfigurations.put(
                                        cacheName, redisCacheConfiguration.entryTtl(ttl)));
        return RedisCacheManager.RedisCacheManagerBuilder
                // Redis 连接工厂
                .fromConnectionFactory(Objects.requireNonNull(redisTemplate.getConnectionFactory()))
                // 缓存配置
                .cacheDefaults(redisCacheConfiguration)
                .withInitialCacheConfigurations(initialConfigurations)
                // 事务感知
                .transactionAware()
                .build();
    }
}
