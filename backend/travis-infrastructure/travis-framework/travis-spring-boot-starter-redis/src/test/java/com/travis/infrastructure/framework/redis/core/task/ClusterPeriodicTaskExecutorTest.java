package com.travis.infrastructure.framework.redis.core.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.travis.infrastructure.framework.redis.core.key.RedisKeyPrefixResolver;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@SuppressWarnings("unchecked")
class ClusterPeriodicTaskExecutorTest {

    private final RedissonClient redissonClient = mock(RedissonClient.class);
    private final RLock lock = mock(RLock.class);
    private final RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
    private final ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);
    private final RedisKeyPrefixResolver keyPrefixResolver = mock(RedisKeyPrefixResolver.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-09T10:02:00Z"), ZoneOffset.UTC);
    private final ClusterPeriodicTaskExecutor executor =
            new ClusterPeriodicTaskExecutor(
                    redissonClient, redisTemplate, keyPrefixResolver, clock);

    @BeforeEach
    void setUp() {
        when(keyPrefixResolver.apply(anyString()))
                .thenAnswer(invocation -> "test:" + invocation.getArgument(0));
        when(keyPrefixResolver.applyLock(anyString(), anyString()))
                .thenAnswer(
                        invocation ->
                                "test:lock:"
                                        + invocation.getArgument(0)
                                        + ':'
                                        + invocation.getArgument(1));
        when(redissonClient.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldRecordSlotOnlyAfterSuccessfulExecution() {
        var count = new AtomicInteger();

        boolean executed =
                executor.executeOncePerInterval(
                        "system-message",
                        "reconcile",
                        Duration.ofMinutes(5),
                        count::incrementAndGet);

        assertThat(executed).isTrue();
        assertThat(count).hasValue(1);
        verify(valueOperations)
                .set(
                        "test:cluster-task:last-success:system-message:reconcile",
                        1_786_269_720_000L,
                        Duration.ofMinutes(5));
        verify(lock).unlock();
    }

    @Test
    void shouldSkipWhileLastSuccessKeyHasNotExpired() {
        when(redisTemplate.hasKey(anyString())).thenReturn(true);
        var count = new AtomicInteger();

        boolean executed =
                executor.executeOncePerInterval(
                        "system-message",
                        "reconcile",
                        Duration.ofMinutes(5),
                        count::incrementAndGet);

        assertThat(executed).isFalse();
        assertThat(count).hasValue(0);
        verify(valueOperations, never())
                .set(
                        anyString(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(Duration.class));
    }

    @Test
    void shouldNotRecordSlotWhenExecutionFails() {
        var failure = new IllegalStateException("failed");

        assertThatThrownBy(
                        () ->
                                executor.executeOncePerInterval(
                                        "system-message",
                                        "reconcile",
                                        Duration.ofMinutes(5),
                                        () -> {
                                            throw failure;
                                        }))
                .isSameAs(failure);
        verify(valueOperations, never())
                .set(
                        anyString(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(Duration.class));
        verify(lock).unlock();
    }

    @Test
    void shouldSkipWhenAnotherNodeOwnsLock() {
        when(lock.tryLock()).thenReturn(false);
        var count = new AtomicInteger();

        boolean executed =
                executor.executeOncePerInterval(
                        "system-message",
                        "reconcile",
                        Duration.ofMinutes(5),
                        count::incrementAndGet);

        assertThat(executed).isFalse();
        assertThat(count).hasValue(0);
        verify(redisTemplate, never()).hasKey(anyString());
    }
}
