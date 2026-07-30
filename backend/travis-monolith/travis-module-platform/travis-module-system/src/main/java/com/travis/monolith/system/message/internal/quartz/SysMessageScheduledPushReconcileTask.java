package com.travis.monolith.system.message.internal.quartz;

import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.framework.redis.core.util.RedisUtil;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 集群内每分钟仅执行一次消息定时任务对账。 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SysMessageScheduledPushReconcileTask {
    private static final String SLOT_KEY_PREFIX = "system:message:scheduled-push:reconcile:";
    private static final long SLOT_TTL_MILLIS = 120_000;

    private final SysMessageScheduledPushScheduler scheduler;

    /** 使用分钟时间槽去重后执行全局加锁对账。 */
    @Scheduled(initialDelay = 60_000, fixedDelay = 60_000)
    public void run() {
        long minuteSlot = Instant.now().getEpochSecond() / 60;
        if (!RedisUtil.setIfAbsent(SLOT_KEY_PREFIX + minuteSlot, Boolean.TRUE, SLOT_TTL_MILLIS)) {
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
