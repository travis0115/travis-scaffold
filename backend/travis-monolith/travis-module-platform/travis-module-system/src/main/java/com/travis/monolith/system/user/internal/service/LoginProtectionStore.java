package com.travis.monolith.system.user.internal.service;

import com.travis.infrastructure.framework.redis.core.util.RedisUtil;
import org.springframework.stereotype.Component;

/** 登录保护 Redis 存储适配器。 */
@Component
public class LoginProtectionStore {

    /** 判断临时锁是否存在。 */
    public boolean isLocked(String key) {
        return Boolean.TRUE.equals(RedisUtil.hasKey(key));
    }

    /** 增加失败计数并刷新 TTL。 */
    public long increment(String key, long ttlMillis) {
        return RedisUtil.incrementAndExpire(key, 1, ttlMillis);
    }

    /** 写入临时锁。 */
    public void lock(String key, long ttlMillis) {
        RedisUtil.set(key, Boolean.TRUE, ttlMillis);
    }

    /** 删除失败计数或临时锁。 */
    public void delete(String key) {
        RedisUtil.delete(key);
    }
}
