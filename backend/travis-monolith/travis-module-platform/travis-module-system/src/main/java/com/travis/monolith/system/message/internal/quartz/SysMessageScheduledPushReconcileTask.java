package com.travis.monolith.system.message.internal.quartz;

import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.framework.redis.core.util.RedisUtil;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 集群内每五分钟仅执行一次消息定时任务对账。 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SysMessageScheduledPushReconcileTask {
    private static final long RECONCILE_INTERVAL_MILLIS = 300_000;
    private static final long SLOT_TTL_MILLIS = 600_000;

    private final SysMessageScheduledPushScheduler scheduler;

    /** 使用五分钟时间槽去重后执行全局加锁对账。 */
    @Scheduled(initialDelay = RECONCILE_INTERVAL_MILLIS, fixedDelay = RECONCILE_INTERVAL_MILLIS)
    public void run() {
        long timeSlot = Instant.now().toEpochMilli() / RECONCILE_INTERVAL_MILLIS;
        if (!RedisUtil.setIfAbsent(
                SysMessageScheduledPushNames.RECONCILE_SLOT_KEY_PREFIX + timeSlot,
                Boolean.TRUE,
                SLOT_TTL_MILLIS)) {
            return;
        }
        try {
            scheduler.reconcile();
        } catch (BizException exception) {
            if (CommonErrorCode.DISTRIBUTED_LOCK_FAILED.equals(exception.getErrorCode())) {
                return;
            }
            log.error("[消息调度] 对账补齐一次性任务失败", exception);
        } catch (Exception exception) {
            log.error("[消息调度] 对账补齐一次性任务失败", exception);
        }
    }
}
