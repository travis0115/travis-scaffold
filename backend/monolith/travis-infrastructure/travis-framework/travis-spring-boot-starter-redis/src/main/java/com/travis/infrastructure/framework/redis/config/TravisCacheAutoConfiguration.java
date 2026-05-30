package com.travis.infrastructure.framework.redis.config;

import cn.hutool.core.util.StrUtil;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.cache.autoconfigure.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.util.Objects;


/**
 * Cache 配置类，基于Redis
 *
 * @author travis
 */
@AutoConfiguration(after = TravisRedisAutoConfiguration.class)
@EnableCaching
@EnableConfigurationProperties({CacheProperties.class})
public class TravisCacheAutoConfiguration {

    /**
     * 基于Redis的缓存配置
     */
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties, RedisTemplate<String,
            Object> redisTemplate) {
        var config = RedisCacheConfiguration.defaultCacheConfig();
        var redisProperties = cacheProperties.getRedis();
        //设置默认缓存时间
        if (redisProperties.getTimeToLive() != null) {
            config = config.entryTtl(redisProperties.getTimeToLive());
        }
        //不缓存null
        if (!redisProperties.isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }
        //自定义缓存前缀名
        config = config.computePrefixWith(cacheName -> {
            var keyPrefix = cacheProperties.getRedis().getKeyPrefix();
            if (!redisProperties.isUseKeyPrefix()) {
                keyPrefix = null;
            }
            if (StrUtil.isNotBlank(keyPrefix)) {
                keyPrefix = keyPrefix.lastIndexOf(StrUtil.COLON) == -1 ? keyPrefix + StrUtil.COLON : keyPrefix;
                return keyPrefix + cacheName + StrUtil.COLON;
            }
            return cacheName + StrUtil.COLON;
        });
        // 设置使用 JSON 序列化方式
        config =
                config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisTemplate.getValueSerializer()));
        return config;
    }

    /**
     * 配置缓存管理器
     */
    @Bean
    public CacheManager cacheManager(RedisCacheConfiguration redisCacheConfiguration,
                                     RedisTemplate<String, Object> redisTemplate) {
        return RedisCacheManager.RedisCacheManagerBuilder
                //Redis 连接工厂
                .fromConnectionFactory(Objects.requireNonNull(redisTemplate.getConnectionFactory()))
                //缓存配置
                .cacheDefaults(redisCacheConfiguration)
                //事务感知
                .transactionAware()
                .build();
    }
}
