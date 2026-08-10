package com.travis.infrastructure.framework.redis.core.key;

import com.travis.infrastructure.framework.redis.config.properties.TravisRedisProperties;
import lombok.Getter;
import org.springframework.util.StringUtils;

/** Redis 项目级 key 前缀解析器。 */
@SuppressWarnings("ClassCanBeRecord")
@Getter
public class RedisKeyPrefixResolver {

    /** -- GETTER -- 获取规范化后的项目级 Redis 键前缀。 */
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

    /** 构造并添加项目前缀的分布式锁键。 */
    public String applyLock(String namespace, String key) {
        return apply("lock:" + namespace + ':' + key);
    }

    /** 从 Redis 键中移除项目级前缀。 */
    public String remove(String key) {
        if (!StringUtils.hasText(keyPrefix) || key == null || !key.startsWith(keyPrefix)) {
            return key;
        }
        return key.substring(keyPrefix.length());
    }

    private String normalize(String prefix) {
        if (!StringUtils.hasText(prefix)) {
            return "";
        }
        return prefix.endsWith(":") ? prefix : prefix + ":";
    }
}
