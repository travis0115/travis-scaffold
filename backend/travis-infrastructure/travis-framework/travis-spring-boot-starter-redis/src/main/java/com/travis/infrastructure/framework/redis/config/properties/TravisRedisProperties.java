package com.travis.infrastructure.framework.redis.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Travis Redis 通用配置。 */
@Data
@ConfigurationProperties(prefix = "travis.redis")
public class TravisRedisProperties {

    /** 项目级 Redis key 前缀。 */
    private String keyPrefix;
}
