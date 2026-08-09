package com.travis.infrastructure.framework.redis.core.task;

import com.travis.infrastructure.framework.redis.core.key.RedisKeyPrefixResolver;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

/** 使用分布式锁和 Redis 过期时间，限制集群周期任务的执行频率。 */
public class ClusterPeriodicTaskExecutor {

    private static final String LAST_SUCCESS_KEY_PREFIX = "cluster-task:last-success:";

    private final RedissonClient redissonClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisKeyPrefixResolver keyPrefixResolver;
    private final Clock clock;

    public ClusterPeriodicTaskExecutor(
            RedissonClient redissonClient,
            RedisTemplate<String, Object> redisTemplate,
            RedisKeyPrefixResolver keyPrefixResolver) {
        this(redissonClient, redisTemplate, keyPrefixResolver, Clock.systemUTC());
    }

    ClusterPeriodicTaskExecutor(
            RedissonClient redissonClient,
            RedisTemplate<String, Object> redisTemplate,
            RedisKeyPrefixResolver keyPrefixResolver,
            Clock clock) {
        this.redissonClient = redissonClient;
        this.redisTemplate = redisTemplate;
        this.keyPrefixResolver = keyPrefixResolver;
        this.clock = clock;
    }

    /**
     * 尝试执行当前周期任务。
     *
     * @return 本次是否实际执行并成功完成
     */
    public boolean executeOncePerInterval(
            String namespace, String taskName, Duration interval, Runnable action) {
        validate(namespace, taskName, interval, action);
        var lock = redissonClient.getLock(keyPrefixResolver.applyLock(namespace, taskName));
        if (!lock.tryLock()) {
            return false;
        }
        try {
            String lastSuccessKey =
                    keyPrefixResolver.apply(LAST_SUCCESS_KEY_PREFIX + namespace + ':' + taskName);
            if (Boolean.TRUE.equals(redisTemplate.hasKey(lastSuccessKey))) {
                return false;
            }
            action.run();
            redisTemplate
                    .opsForValue()
                    .set(lastSuccessKey, Instant.now(clock).toEpochMilli(), interval);
            return true;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void validate(String namespace, String taskName, Duration interval, Runnable action) {
        if (!StringUtils.hasText(namespace)) {
            throw new IllegalArgumentException("集群周期任务命名空间不能为空");
        }
        if (!StringUtils.hasText(taskName)) {
            throw new IllegalArgumentException("集群周期任务名称不能为空");
        }
        if (interval == null || interval.isZero() || interval.isNegative()) {
            throw new IllegalArgumentException("集群周期任务间隔必须大于 0");
        }
        Objects.requireNonNull(action, "集群周期任务执行动作不能为空");
    }
}
