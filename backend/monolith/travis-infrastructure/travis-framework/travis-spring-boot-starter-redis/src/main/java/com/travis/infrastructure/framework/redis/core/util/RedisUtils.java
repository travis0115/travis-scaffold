package com.travis.infrastructure.framework.redis.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 基于 RedisTemplate 的 Redis 工具类，提供静态方法封装常用操作。
 *
 * @author travis
 */
public class RedisUtils {

    private static final Logger log = LoggerFactory.getLogger(RedisUtils.class);

    private static RedisTemplate<String, Object> redisTemplate;

    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        RedisUtils.redisTemplate = redisTemplate;
    }

    /**
     * 指定 key 的过期时间
     *
     * @param key  键
     * @param time 时间（毫秒）
     */
    public static void setExpire(String key, long time) {
        try {
            redisTemplate.expire(key, time, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.warn("redis setExpire failed, key={}", key, e);
            throw new IllegalStateException("redis setExpire failed: " + key, e);
        }
    }

    /**
     * 获取 key 的剩余过期时间
     *
     * @param key 键，不能为 null
     * @return 剩余时间（毫秒），0 表示永久或 key 不存在
     */
    public static Long getExpire(String key) {
        try {
            return redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.warn("redis getExpire failed, key={}", key, e);
            throw new IllegalStateException("redis getExpire failed: " + key, e);
        }
    }

    /**
     * 判断 key 是否存在
     *
     * @param key 键
     * @return true 存在，false 不存在
     */
    public static Boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.warn("redis hasKey failed, key={}", key, e);
            throw new IllegalStateException("redis hasKey failed: " + key, e);
        }
    }

    /**
     * 删除一个或多个 key
     *
     * @param key 键，可多个
     */
    public static void delete(String... key) {
        try {
            if (key != null && key.length > 0) {
                if (key.length == 1) {
                    redisTemplate.delete(key[0]);
                } else {
                    redisTemplate.delete(Arrays.asList(key));
                }
            }
        } catch (Exception e) {
            log.warn("redis delete failed, keys={}", key, e);
            throw new IllegalStateException("redis delete failed", e);
        }
    }

    /**
     * 按模式匹配删除 key（慎用 keys，大 key 集合时阻塞）
     *
     * @param pattern 模式，如 "user:*"
     */
    public static void deleteByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (!CollectionUtils.isEmpty(keys)) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("redis deleteByPattern failed, pattern={}", pattern, e);
            throw new IllegalStateException("redis deleteByPattern failed: " + pattern, e);
        }
    }

    /**
     * 获取 value
     *
     * @param key 键
     * @return 值，不存在为 null
     */
    public static Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("redis get failed, key={}", key, e);
            throw new IllegalStateException("redis get failed: " + key, e);
        }
    }

    /**
     * 设置 value，无过期时间
     *
     * @param key   键
     * @param value 值
     */
    public static void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.warn("redis set failed, key={}", key, e);
            throw new IllegalStateException("redis set failed: " + key, e);
        }
    }

    /**
     * 设置 value 并指定过期时间
     *
     * @param key   键
     * @param value 值
     * @param time  过期时间（毫秒），&lt;=0 表示不设过期
     */
    public static void set(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.MILLISECONDS);
            } else {
                redisTemplate.opsForValue().set(key, value);
            }
        } catch (Exception e) {
            log.warn("redis set failed, key={}", key, e);
            throw new IllegalStateException("redis set failed: " + key, e);
        }
    }

    /**
     * 仅当 key 不存在时设置 value
     *
     * @param key   键
     * @param value 值
     * @return 是否设置成功
     */
    public static boolean setIfAbsent(String key, Object value) {
        try {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.warn("redis setIfAbsent failed, key={}", key, e);
            throw new IllegalStateException("redis setIfAbsent failed: " + key, e);
        }
    }

    /**
     * 仅当 key 不存在时设置 value 并指定过期时间
     *
     * @param key   键
     * @param value 值
     * @param time  过期时间（毫秒）
     * @return 是否设置成功
     */
    public static boolean setIfAbsent(String key, Object value, long time) {
        try {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, time, TimeUnit.MILLISECONDS);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.warn("redis setIfAbsent failed, key={}", key, e);
            throw new IllegalStateException("redis setIfAbsent failed: " + key, e);
        }
    }

    /**
     * value 递增
     *
     * @param key   键
     * @param delta 递增值（可为负）
     * @return 递增后的值
     */
    public static Long increment(String key, long delta) {
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.warn("redis increment failed, key={}", key, e);
            throw new IllegalStateException("redis increment failed: " + key, e);
        }
    }

    /**
     * value 递减
     *
     * @param key   键
     * @param delta 递减量
     * @return 递减后的值
     */
    public static Long decrement(String key, long delta) {
        try {
            return redisTemplate.opsForValue().decrement(key, delta);
        } catch (Exception e) {
            log.warn("redis decrement failed, key={}", key, e);
            throw new IllegalStateException("redis decrement failed: " + key, e);
        }
    }
}
