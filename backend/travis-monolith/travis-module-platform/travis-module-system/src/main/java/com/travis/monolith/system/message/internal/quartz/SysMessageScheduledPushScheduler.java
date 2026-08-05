package com.travis.monolith.system.message.internal.quartz;

import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.redis.core.annotation.DistributedLock;
import com.travis.infrastructure.framework.redis.core.annotation.DistributedLockNamespace;
import com.travis.monolith.system.common.api.enums.SystemErrorCode;
import com.travis.monolith.system.message.api.enums.SysMessagePushType;
import com.travis.monolith.system.message.api.enums.SysMessageStatus;
import com.travis.monolith.system.message.internal.entity.SysMessage;
import com.travis.monolith.system.message.internal.mapper.SysMessageMapper;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Component;

/** 负责维护消息发布时间对应的一次性 Quartz 任务。 */
@Component
@AllArgsConstructor
@DistributedLockNamespace(SysMessageScheduledPushNames.LOCK_NAMESPACE)
@Slf4j
public class SysMessageScheduledPushScheduler {
    private static final JobKey LEGACY_JOB_KEY =
            JobKey.jobKey(
                    SysMessageScheduledPushNames.LEGACY_JOB_NAME,
                    SysMessageScheduledPushNames.QUARTZ_GROUP);
    private static final int RECONCILE_BATCH_SIZE = 500;

    private final Scheduler scheduler;
    private final SysMessageMapper messageMapper;

    /** 清理旧版固定扫描任务。 */
    public void initialize() {
        try {
            scheduler.deleteJob(LEGACY_JOB_KEY);
        } catch (BizException exception) {
            throw exception;
        } catch (Exception exception) {
            throw schedulerError(exception);
        }
    }

    /** 在全局分布式锁内补齐缺失的一次性任务。 */
    @DistributedLock(key = SysMessageScheduledPushNames.RECONCILE_LOCK_KEY_SPEL)
    public void reconcile() {
        reconcileMissingTriggers();
    }

    /** 根据消息当前数据库状态创建、重排或删除一次性任务。 */
    @DistributedLock(key = SysMessageScheduledPushNames.RECONCILE_LOCK_KEY_SPEL)
    public void sync(Long messageId) {
        var message =
                messageMapper.selectOne(
                        new LambdaQueryWrapperX<SysMessage>()
                                .select(
                                        SysMessage::getId,
                                        SysMessage::getPublishTime,
                                        SysMessage::getPushType,
                                        SysMessage::getStatus)
                                .eq(SysMessage::getId, messageId));
        if (message != null
                && SysMessageStatus.PENDING.getValue().equals(message.getStatus())
                && SysMessagePushType.SCHEDULED.getValue().equals(message.getPushType())
                && message.getPublishTime() != null) {
            schedule(messageId, message.getPublishTime());
            return;
        }
        delete(messageId);
    }

    /** 创建或重排指定消息的一次性任务。 */
    private void schedule(Long messageId, LocalDateTime publishTime) {
        try {
            JobKey jobKey = jobKey(messageId);
            Trigger trigger = buildTrigger(messageId, publishTime);
            if (scheduler.checkExists(jobKey)) {
                synchronizeExistingJob(jobKey, trigger, publishTime);
                return;
            }
            try {
                scheduler.scheduleJob(buildJobDetail(messageId), trigger);
            } catch (ObjectAlreadyExistsException exception) {
                if (!scheduler.checkExists(jobKey)) {
                    throw exception;
                }
                synchronizeExistingJob(jobKey, trigger, publishTime);
            }
        } catch (Exception exception) {
            throw schedulerError(exception);
        }
    }

    /** 删除指定消息的一次性任务。 */
    private void delete(Long messageId) {
        try {
            scheduler.deleteJob(jobKey(messageId));
        } catch (Exception exception) {
            throw schedulerError(exception);
        }
    }

    private void reconcileMissingTriggers() {
        try {
            Set<JobKey> expectedJobKeys = new java.util.HashSet<>();
            Set<TriggerKey> expectedTriggerKeys = new java.util.HashSet<>();
            Set<TriggerKey> existingTriggerKeys =
                    java.util.Optional.ofNullable(
                                    scheduler.getTriggerKeys(
                                            GroupMatcher.triggerGroupEquals(
                                                    SysMessageScheduledPushNames.QUARTZ_GROUP)))
                            .orElseGet(Set::of);
            Long afterMessageId = null;
            while (true) {
                var wrapper =
                        new LambdaQueryWrapperX<SysMessage>()
                                .select(SysMessage::getId, SysMessage::getPublishTime)
                                .eq(SysMessage::getStatus, SysMessageStatus.PENDING.getValue())
                                .eq(
                                        SysMessage::getPushType,
                                        SysMessagePushType.SCHEDULED.getValue())
                                .isNotNull(SysMessage::getPublishTime)
                                .gt(afterMessageId != null, SysMessage::getId, afterMessageId)
                                .orderByAsc(SysMessage::getId)
                                .last("LIMIT " + RECONCILE_BATCH_SIZE);
                var messages = messageMapper.selectList(wrapper);
                for (SysMessage message : messages) {
                    JobKey expectedJobKey = jobKey(message.getId());
                    TriggerKey expectedTriggerKey =
                            triggerKey(message.getId(), message.getPublishTime());
                    expectedJobKeys.add(expectedJobKey);
                    expectedTriggerKeys.add(expectedTriggerKey);
                    Trigger existingTrigger =
                            existingTriggerKeys.contains(expectedTriggerKey)
                                    ? scheduler.getTrigger(expectedTriggerKey)
                                    : null;
                    if (!isTriggerCurrent(existingTrigger, message.getPublishTime())) {
                        schedule(message.getId(), message.getPublishTime());
                    }
                }
                if (messages.size() < RECONCILE_BATCH_SIZE) {
                    break;
                }
                afterMessageId = messages.getLast().getId();
            }
            var existingJobKeys =
                    scheduler.getJobKeys(
                            GroupMatcher.jobGroupEquals(SysMessageScheduledPushNames.QUARTZ_GROUP));
            if (existingJobKeys != null) {
                for (JobKey existingJobKey : existingJobKeys) {
                    if (!expectedJobKeys.contains(existingJobKey)) {
                        scheduler.deleteJob(existingJobKey);
                    }
                }
            }
            if (existingTriggerKeys != null) {
                existingTriggerKeys.stream()
                        .filter(
                                existingTriggerKey ->
                                        !expectedTriggerKeys.contains(existingTriggerKey))
                        .forEach(this::unschedule);
            }
        } catch (BizException exception) {
            throw exception;
        } catch (Exception exception) {
            throw schedulerError(exception);
        }
    }

    private org.quartz.JobDetail buildJobDetail(Long messageId) {
        return JobBuilder.newJob(SysMessageScheduledPushJob.class)
                .withIdentity(jobKey(messageId))
                .withDescription("推送定时消息 " + messageId)
                .usingJobData(SysMessageScheduledPushJob.DATA_MESSAGE_ID, messageId)
                .build();
    }

    private Trigger buildTrigger(Long messageId, LocalDateTime publishTime) {
        return TriggerBuilder.newTrigger()
                .withIdentity(triggerKey(messageId, publishTime))
                .forJob(jobKey(messageId))
                .startAt(toDate(publishTime))
                .withSchedule(
                        SimpleScheduleBuilder.simpleSchedule()
                                .withRepeatCount(0)
                                .withMisfireHandlingInstructionFireNow())
                .build();
    }

    private Date toDate(LocalDateTime publishTime) {
        return Date.from(publishTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private JobKey jobKey(Long messageId) {
        return JobKey.jobKey(
                SysMessageScheduledPushNames.JOB_NAME_PREFIX + messageId,
                SysMessageScheduledPushNames.QUARTZ_GROUP);
    }

    private TriggerKey triggerKey(Long messageId, LocalDateTime publishTime) {
        return TriggerKey.triggerKey(
                SysMessageScheduledPushNames.TRIGGER_NAME_PREFIX
                        + messageId
                        + '-'
                        + toDate(publishTime).getTime(),
                SysMessageScheduledPushNames.QUARTZ_GROUP);
    }

    /** 使指定 Job 仅保留与当前发布时间一致的 Trigger。 */
    private void synchronizeExistingJob(
            JobKey jobKey, Trigger expectedTrigger, LocalDateTime publishTime)
            throws SchedulerException {
        var triggers = scheduler.getTriggersOfJob(jobKey);
        var matchingTrigger =
                triggers.stream()
                        .filter(existing -> existing.getKey().equals(expectedTrigger.getKey()))
                        .findFirst()
                        .orElse(null);
        if (matchingTrigger != null) {
            if (!isTriggerCurrent(matchingTrigger, publishTime)
                    && scheduler.rescheduleJob(matchingTrigger.getKey(), expectedTrigger) == null) {
                scheduler.scheduleJob(expectedTrigger);
            }
            triggers.stream()
                    .filter(existing -> !existing.getKey().equals(expectedTrigger.getKey()))
                    .map(Trigger::getKey)
                    .forEach(this::unschedule);
            return;
        }
        if (triggers.isEmpty()) {
            scheduler.scheduleJob(expectedTrigger);
            return;
        }
        if (scheduler.rescheduleJob(triggers.getFirst().getKey(), expectedTrigger) == null) {
            scheduler.scheduleJob(expectedTrigger);
        }
        triggers.stream().skip(1).map(Trigger::getKey).forEach(this::unschedule);
    }

    private boolean isTriggerCurrent(Trigger trigger, LocalDateTime publishTime) {
        return trigger != null
                && trigger.getNextFireTime() != null
                && trigger.getNextFireTime().getTime() == toDate(publishTime).getTime();
    }

    private void unschedule(TriggerKey triggerKey) {
        try {
            scheduler.unscheduleJob(triggerKey);
        } catch (Exception exception) {
            throw schedulerError(exception);
        }
    }

    private BizException schedulerError(Exception exception) {
        if (exception instanceof BizException bizException) {
            return bizException;
        }
        return new BizException(
                SystemErrorCode.MESSAGE_SCHEDULER_ERROR, exception, exception.getMessage());
    }
}
