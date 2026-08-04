package com.travis.infrastructure.framework.redis.config.properties;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Travis Redis 通用配置。 */
@Data
@ConfigurationProperties(prefix = "travis.redis")
public class TravisRedisProperties {

    /** 项目级 Redis key 前缀。 */
    private String keyPrefix;

    /** 按缓存名称配置的过期时间。 */
    private Map<String, Duration> cacheTtl = new LinkedHashMap<>();
}
