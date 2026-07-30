package com.travis.monolith.system.message.internal.quartz;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.travis.infrastructure.framework.redis.core.util.RedisUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class SysMessageScheduledPushReconcileTaskTest {

    @Test
    void shouldSkipReconciliationWhenMinuteSlotWasAlreadyClaimed() {
        var scheduler = mock(SysMessageScheduledPushScheduler.class);
        var task = new SysMessageScheduledPushReconcileTask(scheduler);
        try (MockedStatic<RedisUtil> redisUtil = Mockito.mockStatic(RedisUtil.class)) {
            redisUtil
                    .when(
                            () ->
                                    RedisUtil.setIfAbsent(
                                            any(String.class), eq(Boolean.TRUE), anyLong()))
                    .thenReturn(false);

            task.run();

            verify(scheduler, never()).reconcile();
        }
    }

    @Test
    void shouldReconcileAfterClaimingMinuteSlot() {
        var scheduler = mock(SysMessageScheduledPushScheduler.class);
        var task = new SysMessageScheduledPushReconcileTask(scheduler);
        try (MockedStatic<RedisUtil> redisUtil = Mockito.mockStatic(RedisUtil.class)) {
            redisUtil
                    .when(
                            () ->
                                    RedisUtil.setIfAbsent(
                                            any(String.class), eq(Boolean.TRUE), anyLong()))
                    .thenReturn(true);

            task.run();

            verify(scheduler).reconcile();
        }
    }
}
