package com.travis.infrastructure.framework.redis.core.key;

import com.travis.infrastructure.framework.redis.config.properties.TravisRedisProperties;
import org.springframework.util.StringUtils;

/** Redis 项目级 key 前缀解析器。 */
public class RedisKeyPrefixResolver {

    private final String keyPrefix;

    public RedisKeyPrefixResolver(TravisRedisProperties properties) {
        this.keyPrefix = normalize(properties.getKeyPrefix());
    }

    /** 为原始 Redis 键添加项目级前缀。 */
    public String apply(String key) {
        if (!StringUtils.hasText(keyPrefix) || !StringUtils.hasText(key)) {
            return key;
        }
        if (key.startsWith(keyPrefix)) {
            return key;
        }
        return keyPrefix + key;
    }

    /** 从 Redis 键中移除项目级前缀。 */
    public String remove(String key) {
        if (!StringUtils.hasText(keyPrefix) || key == null || !key.startsWith(keyPrefix)) {
            return key;
        }
        return key.substring(keyPrefix.length());
    }

    /** 获取规范化后的项目级 Redis 键前缀。 */
    public String getKeyPrefix() {
        return keyPrefix;
    }

    private String normalize(String prefix) {
        if (!StringUtils.hasText(prefix)) {
            return "";
        }
        return prefix.endsWith(":") ? prefix : prefix + ":";
    }
}
