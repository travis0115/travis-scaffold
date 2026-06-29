package com.travis.infrastructure.framework.redis.core.util;

import com.travis.infrastructure.framework.redis.core.key.RedisKeyPrefixResolver;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;

/**
 * 基于 RedisTemplate 的 Redis 工具类，提供静态方法封装常用操作。
 *
 * @author travis
 */
@Slf4j
public class RedisUtil {

    private static RedisTemplate<String, Object> redisTemplate;

    private static ObjectProvider<CacheKeyPrefix> cacheKeyPrefixProvider;

    private static RedisKeyPrefixResolver redisKeyPrefixResolver;

    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        RedisUtil.redisTemplate = redisTemplate;
    }

    public void setCacheKeyPrefixProvider(ObjectProvider<CacheKeyPrefix> cacheKeyPrefixProvider) {
        RedisUtil.cacheKeyPrefixProvider = cacheKeyPrefixProvider;
    }

    public void setRedisKeyPrefixResolver(RedisKeyPrefixResolver redisKeyPrefixResolver) {
        RedisUtil.redisKeyPrefixResolver = redisKeyPrefixResolver;
    }

    /**
     * 指定 key 的过期时间
     *
     * @param key 键
     * @param time 时间（毫秒）
     */
    public static void setExpire(String key, long time) {
        try {
            redisTemplate.expire(resolveKey(key), Expiration.from(time, TimeUnit.MILLISECONDS));
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
            return redisTemplate.getExpire(resolveKey(key), TimeUnit.MILLISECONDS);
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
            return redisTemplate.hasKey(resolveKey(key));
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
        if (key == null || key.length == 0) {
            return;
        }
        delete(Arrays.asList(key));
    }

    /**
     * 删除多个 key
     *
     * @param keys 键集合
     */
    public static void delete(Collection<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }
        try {
            var resolvedKeys = keys.stream().map(RedisUtil::resolveKey).toList();
            if (keys.size() == 1) {
                redisTemplate.delete(resolvedKeys.getFirst());
            } else {
                redisTemplate.delete(resolvedKeys);
            }
        } catch (Exception e) {
            log.warn("redis delete failed, keys={}", keys, e);
            throw new IllegalStateException("redis delete failed", e);
        }
    }

    /**
     * 删除 Spring Cache 生成的 Redis key，会自动套用当前 CacheKeyPrefix 配置。
     *
     * @param cacheName 缓存名称
     * @param key 缓存 key，可多个
     */
    public static void deleteCacheKey(String cacheName, String... key) {
        if (key == null || key.length == 0) {
            return;
        }
        deleteCacheKey(cacheName, Arrays.asList(key));
    }

    /**
     * 删除 Spring Cache 生成的 Redis key，会自动套用当前 CacheKeyPrefix 配置。
     *
     * @param cacheName 缓存名称
     * @param keys 缓存 key 集合
     */
    public static void deleteCacheKey(String cacheName, Collection<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }
        runAfterCommitIfNecessary(() -> doDeleteCacheKey(cacheName, keys));
    }

    private static void doDeleteCacheKey(String cacheName, Collection<String> keys) {
        try {
            var cacheKeys = keys.stream().map(item -> buildCacheKey(cacheName, item)).toList();
            if (cacheKeys.size() == 1) {
                redisTemplate.delete(cacheKeys.getFirst());
            } else {
                redisTemplate.delete(cacheKeys);
            }
        } catch (Exception e) {
            log.warn("redis delete cache key failed, cacheName={}, keys={}", cacheName, keys, e);
            throw new IllegalStateException("redis delete cache key failed", e);
        }
    }

    private static String buildCacheKey(String cacheName, String key) {
        var cacheKeyPrefix =
                cacheKeyPrefixProvider == null ? null : cacheKeyPrefixProvider.getIfAvailable();
        var prefix = cacheKeyPrefix == null ? cacheName + ":" : cacheKeyPrefix.compute(cacheName);
        return prefix + key;
    }

    /**
     * 按模式匹配删除 key（慎用 keys，大 key 集合时阻塞）
     *
     * @param pattern 模式，如 "user:*"
     */
    public static void deleteByPattern(String pattern) {
        if (pattern == null) {
            return;
        }
        deleteByPattern(Set.of(pattern));
    }

    /**
     * 按模式匹配删除 key（慎用 keys，大 key 集合时阻塞）
     *
     * @param patterns 模式集合，如 "user:*"
     */
    public static void deleteByPattern(Collection<String> patterns) {
        if (CollectionUtils.isEmpty(patterns)) {
            return;
        }
        try {
            for (String pattern : patterns) {
                doDeleteByPattern(pattern);
            }
        } catch (Exception e) {
            log.warn("redis deleteByPattern failed, patterns={}", patterns, e);
            throw new IllegalStateException("redis deleteByPattern failed", e);
        }
    }

    /**
     * 按模式匹配删除 Spring Cache 生成的 Redis key，会自动套用当前 CacheKeyPrefix 配置。
     *
     * @param cacheName 缓存名称
     * @param pattern 缓存 key 模式，如 "detail:*"
     */
    public static void deleteCacheKeyByPattern(String cacheName, String pattern) {
        if (pattern == null) {
            return;
        }
        deleteCacheKeyByPattern(cacheName, Set.of(pattern));
    }

    /**
     * 按模式匹配删除 Spring Cache 生成的 Redis key，会自动套用当前 CacheKeyPrefix 配置。
     *
     * @param cacheName 缓存名称
     * @param patterns 缓存 key 模式集合，如 "detail:*"
     */
    public static void deleteCacheKeyByPattern(String cacheName, Collection<String> patterns) {
        if (CollectionUtils.isEmpty(patterns)) {
            return;
        }
        runAfterCommitIfNecessary(
                () -> {
                    for (String pattern : patterns) {
                        doDeleteCacheKeyByPattern(cacheName, pattern);
                    }
                });
    }

    private static void doDeleteCacheKeyByPattern(String cacheName, String pattern) {
        try {
            doDeleteByResolvedPattern(buildCacheKey(cacheName, pattern));
        } catch (Exception e) {
            log.warn(
                    "redis delete cache key by pattern failed, cacheName={}, pattern={}",
                    cacheName,
                    pattern,
                    e);
            throw new IllegalStateException("redis delete cache key by pattern failed", e);
        }
    }

    private static void doDeleteByPattern(String pattern) {
        doDeleteByResolvedPattern(resolveKey(pattern));
    }

    private static void doDeleteByResolvedPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (!CollectionUtils.isEmpty(keys)) {
            redisTemplate.delete(keys);
        }
    }

    private static void runAfterCommitIfNecessary(Runnable task) {
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            task.run();
                        }
                    });
            return;
        }
        task.run();
    }

    /**
     * 获取 value
     *
     * @param key 键
     * @return 值，不存在为 null
     */
    public static Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(resolveKey(key));
        } catch (Exception e) {
            log.warn("redis get failed, key={}", key, e);
            throw new IllegalStateException("redis get failed: " + key, e);
        }
    }

    /**
     * 设置 value，无过期时间
     *
     * @param key 键
     * @param value 值
     */
    public static void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(resolveKey(key), value);
        } catch (Exception e) {
            log.warn("redis set failed, key={}", key, e);
            throw new IllegalStateException("redis set failed: " + key, e);
        }
    }

    /**
     * 设置 value 并指定过期时间
     *
     * @param key 键
     * @param value 值
     * @param time 过期时间（毫秒），&lt;=0 表示不设过期
     */
    public static void set(String key, Object value, long time) {
        try {
            var resolvedKey = resolveKey(key);
            if (time > 0) {
                redisTemplate.opsForValue().set(resolvedKey, value, time, TimeUnit.MILLISECONDS);
            } else {
                redisTemplate.opsForValue().set(resolvedKey, value);
            }
        } catch (Exception e) {
            log.warn("redis set failed, key={}", key, e);
            throw new IllegalStateException("redis set failed: " + key, e);
        }
    }

    /**
     * 仅当 key 不存在时设置 value
     *
     * @param key 键
     * @param value 值
     * @return 是否设置成功
     */
    public static boolean setIfAbsent(String key, Object value) {
        try {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(resolveKey(key), value);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.warn("redis setIfAbsent failed, key={}", key, e);
            throw new IllegalStateException("redis setIfAbsent failed: " + key, e);
        }
    }

    /**
     * 仅当 key 不存在时设置 value 并指定过期时间
     *
     * @param key 键
     * @param value 值
     * @param time 过期时间（毫秒）
     * @return 是否设置成功
     */
    public static boolean setIfAbsent(String key, Object value, long time) {
        try {
            Boolean result =
                    redisTemplate
                            .opsForValue()
                            .setIfAbsent(resolveKey(key), value, time, TimeUnit.MILLISECONDS);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.warn("redis setIfAbsent failed, key={}", key, e);
            throw new IllegalStateException("redis setIfAbsent failed: " + key, e);
        }
    }

    /**
     * value 递增
     *
     * @param key 键
     * @param delta 递增值（可为负）
     * @return 递增后的值
     */
    public static Long increment(String key, long delta) {
        try {
            return redisTemplate.opsForValue().increment(resolveKey(key), delta);
        } catch (Exception e) {
            log.warn("redis increment failed, key={}", key, e);
            throw new IllegalStateException("redis increment failed: " + key, e);
        }
    }

    /**
     * value 递减
     *
     * @param key 键
     * @param delta 递减量
     * @return 递减后的值
     */
    public static Long decrement(String key, long delta) {
        try {
            return redisTemplate.opsForValue().decrement(resolveKey(key), delta);
        } catch (Exception e) {
            log.warn("redis decrement failed, key={}", key, e);
            throw new IllegalStateException("redis decrement failed: " + key, e);
        }
    }

    /**
     * 向 Set 添加成员
     *
     * @param key 键
     * @param values 成员
     * @return 新增成员数量
     */
    public static Long setAdd(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(resolveKey(key), values);
        } catch (Exception e) {
            log.warn("redis setAdd failed, key={}", key, e);
            throw new IllegalStateException("redis setAdd failed: " + key, e);
        }
    }

    /**
     * 从 Set 删除成员
     *
     * @param key 键
     * @param values 成员
     * @return 删除成员数量
     */
    public static Long setRemove(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().remove(resolveKey(key), values);
        } catch (Exception e) {
            log.warn("redis setRemove failed, key={}", key, e);
            throw new IllegalStateException("redis setRemove failed: " + key, e);
        }
    }

    /**
     * 判断 Set 是否包含成员
     *
     * @param key 键
     * @param value 成员
     * @return true 包含，false 不包含
     */
    public static Boolean setIsMember(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(resolveKey(key), value);
        } catch (Exception e) {
            log.warn("redis setIsMember failed, key={}", key, e);
            throw new IllegalStateException("redis setIsMember failed: " + key, e);
        }
    }

    /**
     * 获取 Set 所有成员
     *
     * @param key 键
     * @return 成员集合
     */
    public static Set<Object> setMembers(String key) {
        try {
            return redisTemplate.opsForSet().members(resolveKey(key));
        } catch (Exception e) {
            log.warn("redis setMembers failed, key={}", key, e);
            throw new IllegalStateException("redis setMembers failed: " + key, e);
        }
    }

    /**
     * 获取 Set 成员数量
     *
     * @param key 键
     * @return 成员数量
     */
    public static Long setSize(String key) {
        try {
            return redisTemplate.opsForSet().size(resolveKey(key));
        } catch (Exception e) {
            log.warn("redis setSize failed, key={}", key, e);
            throw new IllegalStateException("redis setSize failed: " + key, e);
        }
    }

    private static String resolveKey(String key) {
        return redisKeyPrefixResolver == null ? key : redisKeyPrefixResolver.apply(key);
    }
}
