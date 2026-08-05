package com.travis.monolith.system.message.internal.quartz;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.redis.core.annotation.DistributedLock;
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
@Slf4j
public class SysMessageScheduledPushScheduler {
    private static final String GROUP = "system-message";
    private static final JobKey LEGACY_JOB_KEY = JobKey.jobKey("scheduled-message-push", GROUP);
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
    @DistributedLock(namespace = "system-message", key = "'scheduled-push-reconcile'")
    public void reconcile() {
        reconcileMissingTriggers();
    }

    /** 创建或重排指定消息的一次性任务。 */
    public void schedule(Long messageId, LocalDateTime publishTime) {
        try {
            JobKey jobKey = jobKey(messageId);
            Trigger trigger = buildTrigger(messageId, publishTime);
            if (scheduler.checkExists(jobKey)) {
                var triggers = scheduler.getTriggersOfJob(jobKey);
                if (triggers.stream()
                        .anyMatch(existing -> existing.getKey().equals(trigger.getKey()))) {
                    triggers.stream()
                            .filter(existing -> !existing.getKey().equals(trigger.getKey()))
                            .map(Trigger::getKey)
                            .forEach(this::unscheduleQuietly);
                    return;
                }
                if (triggers.isEmpty()) {
                    scheduler.scheduleJob(trigger);
                    return;
                }
                if (scheduler.rescheduleJob(triggers.getFirst().getKey(), trigger) == null) {
                    scheduler.scheduleJob(trigger);
                }
                triggers.stream().skip(1).map(Trigger::getKey).forEach(this::unscheduleQuietly);
                return;
            }
            try {
                scheduler.scheduleJob(buildJobDetail(messageId), trigger);
            } catch (ObjectAlreadyExistsException ignored) {
                schedule(messageId, publishTime);
            }
        } catch (Exception exception) {
            throw schedulerError(exception);
        }
    }

    /** 删除指定消息的一次性任务。 */
    public void delete(Long messageId) {
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
                                            GroupMatcher.triggerGroupEquals(GROUP)))
                            .orElseGet(Set::of);
            long pageNumber = 1;
            while (true) {
                var page =
                        messageMapper.selectPage(
                                new Page<SysMessage>(pageNumber, RECONCILE_BATCH_SIZE, false),
                                new LambdaQueryWrapperX<SysMessage>()
                                        .eq(
                                                SysMessage::getStatus,
                                                SysMessageStatus.PENDING.getValue())
                                        .eq(
                                                SysMessage::getPushType,
                                                SysMessagePushType.SCHEDULED.getValue())
                                        .isNotNull(SysMessage::getPublishTime)
                                        .orderByAsc(SysMessage::getId));
                for (SysMessage message : page.getRecords()) {
                    JobKey expectedJobKey = jobKey(message.getId());
                    TriggerKey expectedTriggerKey =
                            triggerKey(message.getId(), message.getPublishTime());
                    expectedJobKeys.add(expectedJobKey);
                    expectedTriggerKeys.add(expectedTriggerKey);
                    if (!existingTriggerKeys.contains(expectedTriggerKey)) {
                        schedule(message.getId(), message.getPublishTime());
                        continue;
                    }
                    Trigger existingTrigger = scheduler.getTrigger(expectedTriggerKey);
                    Date expectedFireTime = toDate(message.getPublishTime());
                    if (existingTrigger == null
                            || existingTrigger.getNextFireTime() == null
                            || existingTrigger.getNextFireTime().getTime()
                                    != expectedFireTime.getTime()) {
                        if (scheduler.rescheduleJob(
                                        expectedTriggerKey,
                                        buildTrigger(message.getId(), message.getPublishTime()))
                                == null) {
                            schedule(message.getId(), message.getPublishTime());
                        }
                    }
                }
                if (page.getRecords().size() < RECONCILE_BATCH_SIZE) {
                    break;
                }
                pageNumber++;
            }
            var existingJobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(GROUP));
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
                        .forEach(this::unscheduleQuietly);
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
        return JobKey.jobKey("scheduled-message-push-" + messageId, GROUP);
    }

    private TriggerKey triggerKey(Long messageId, LocalDateTime publishTime) {
        return TriggerKey.triggerKey(
                "scheduled-message-push-trigger-" + messageId + '-' + toDate(publishTime).getTime(),
                GROUP);
    }

    private void unscheduleQuietly(TriggerKey triggerKey) {
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
