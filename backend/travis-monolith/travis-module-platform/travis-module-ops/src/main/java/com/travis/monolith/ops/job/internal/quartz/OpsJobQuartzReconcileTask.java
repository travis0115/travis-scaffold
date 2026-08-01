package com.travis.monolith.ops.job.internal.quartz;

import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.framework.redis.core.util.RedisUtil;
import com.travis.monolith.ops.job.internal.service.OpsJobLogService;
import com.travis.monolith.ops.job.internal.service.OpsJobService;
import com.travis.monolith.ops.job.internal.service.QuartzJobManager;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 定期将任务业务配置与 Quartz 持久化状态对账。 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OpsJobQuartzReconcileTask {

    private static final String SLOT_KEY_PREFIX = "ops:job:quartz:reconcile:";
    private static final long SLOT_TTL_MILLIS = 120_000;

    private final OpsJobService jobService;
    private final OpsJobLogService jobLogService;
    private final QuartzJobManager quartzJobManager;

    /** 使用分钟时间槽去重后执行集群对账。 */
    @Scheduled(initialDelay = 60_000, fixedDelay = 60_000)
    public void run() {
        try {
            long minuteSlot = Instant.now().getEpochSecond() / 60;
            if (!RedisUtil.setIfAbsent(
                    SLOT_KEY_PREFIX + minuteSlot, Boolean.TRUE, SLOT_TTL_MILLIS)) {
                return;
            }
            quartzJobManager.reconcile(jobService.listAll());
            jobLogService.markInterruptedExecutions();
        } catch (BizException exception) {
            if (CommonErrorCode.DISTRIBUTED_LOCK_FAILED.equals(exception.getErrorCode())) {
                return;
            }
            log.error("Quartz 任务配置对账失败", exception);
        } catch (Exception exception) {
            log.error("Quartz 任务配置对账失败", exception);
        }
    }
}
