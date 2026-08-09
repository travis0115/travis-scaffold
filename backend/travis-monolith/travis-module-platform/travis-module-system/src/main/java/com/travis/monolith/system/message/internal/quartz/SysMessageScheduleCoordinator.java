package com.travis.monolith.system.message.internal.quartz;

import com.travis.infrastructure.framework.quartz.core.QuartzSyncExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 将消息调度同步提交给 Quartz 公共执行器。 */
@Component
@RequiredArgsConstructor
public class SysMessageScheduleCoordinator {
    private final SysMessageScheduledPushScheduler scheduler;
    private final QuartzSyncExecutor syncExecutor;

    /** 事务提交后根据消息最新数据库状态同步一次性任务。 */
    public void syncAfterCommit(Long messageId) {
        syncExecutor.executeAfterCommit(
                "system-message:" + messageId, () -> scheduler.sync(messageId));
    }
}
