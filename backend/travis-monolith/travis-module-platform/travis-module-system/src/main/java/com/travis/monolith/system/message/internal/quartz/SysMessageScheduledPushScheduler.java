package com.travis.monolith.system.message.internal.quartz;

import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.quartz.core.QuartzOneShotManager;
import com.travis.infrastructure.framework.quartz.core.QuartzOneShotTask;
import com.travis.infrastructure.framework.redis.core.annotation.DistributedLock;
import com.travis.infrastructure.framework.redis.core.annotation.DistributedLockNamespace;
import com.travis.monolith.system.message.api.enums.SysMessagePushType;
import com.travis.monolith.system.message.api.enums.SysMessageStatus;
import com.travis.monolith.system.message.internal.entity.SysMessage;
import com.travis.monolith.system.message.internal.mapper.SysMessageMapper;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** 将消息定时发布状态投影为一次性 Quartz 任务。 */
@Component
@DistributedLockNamespace(SysMessageScheduledPushNames.LOCK_NAMESPACE)
public class SysMessageScheduledPushScheduler {

    private static final int RECONCILE_BATCH_SIZE = 500;

    private final QuartzOneShotManager oneShotManager;
    private final SysMessageMapper messageMapper;

    public SysMessageScheduledPushScheduler(
            QuartzOneShotManager oneShotManager, SysMessageMapper messageMapper) {
        this.oneShotManager = oneShotManager;
        this.messageMapper = messageMapper;
        oneShotManager.registerGroup(
                SysMessageScheduledPushNames.QUARTZ_GROUP,
                SysMessageScheduledPushNames.QUARTZ_GROUP_OWNER);
    }

    /** 使 Quartz 任务与当前数据库状态一致。 */
    public void reconcile() {
        oneShotManager.reconcile(
                SysMessageScheduledPushNames.QUARTZ_GROUP,
                SysMessageScheduledPushNames.QUARTZ_GROUP_OWNER,
                this::scanScheduledMessages);
    }

    /** 分批扫描当前应当存在的消息一次性任务。 */
    private void scanScheduledMessages(Consumer<QuartzOneShotTask> taskConsumer) {
        Long afterMessageId = null;
        while (true) {
            var messages = listScheduledMessages(afterMessageId);
            for (SysMessage message : messages) {
                taskConsumer.accept(toTask(message));
            }
            if (messages.size() < RECONCILE_BATCH_SIZE) {
                break;
            }
            afterMessageId = messages.getLast().getId();
        }
    }

    /** 根据消息当前数据库状态创建、重排或删除一次性任务。 */
    @DistributedLock(key = SysMessageScheduledPushNames.RECONCILE_LOCK_KEY_SPEL)
    public void sync(Long messageId) {
        var message = findScheduledMessage(messageId);
        if (message == null) {
            oneShotManager.delete(SysMessageScheduledPushNames.QUARTZ_GROUP, taskName(messageId));
            return;
        }
        oneShotManager.sync(toTask(message));
    }

    private SysMessage findScheduledMessage(Long messageId) {
        return messageMapper.selectOne(scheduledMessageQuery().eq(SysMessage::getId, messageId));
    }

    private List<SysMessage> listScheduledMessages(Long afterMessageId) {
        return messageMapper.selectList(
                scheduledMessageQuery()
                        .gt(afterMessageId != null, SysMessage::getId, afterMessageId)
                        .orderByAsc(SysMessage::getId)
                        .last("LIMIT " + RECONCILE_BATCH_SIZE));
    }

    private LambdaQueryWrapperX<SysMessage> scheduledMessageQuery() {
        var wrapper = new LambdaQueryWrapperX<SysMessage>();
        wrapper.select(SysMessage::getId, SysMessage::getPublishTime);
        wrapper.eq(SysMessage::getStatus, SysMessageStatus.PENDING.getValue());
        wrapper.eq(SysMessage::getPushType, SysMessagePushType.SCHEDULED.getValue());
        wrapper.isNotNull(SysMessage::getPublishTime);
        return wrapper;
    }

    private QuartzOneShotTask toTask(SysMessage message) {
        return new QuartzOneShotTask(
                SysMessageScheduledPushNames.QUARTZ_GROUP,
                taskName(message.getId()),
                SysMessageScheduledPushJob.class,
                Map.of(SysMessageScheduledPushJob.DATA_MESSAGE_ID, message.getId()),
                message.getPublishTime().atZone(ZoneId.systemDefault()).toInstant(),
                "推送定时消息 " + message.getId());
    }

    private String taskName(Long messageId) {
        return SysMessageScheduledPushNames.JOB_NAME_PREFIX + messageId;
    }
}
