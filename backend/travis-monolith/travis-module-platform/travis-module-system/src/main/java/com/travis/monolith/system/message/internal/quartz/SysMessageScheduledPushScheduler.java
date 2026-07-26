package com.travis.monolith.system.message.internal.quartz;

import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.monolith.system.common.api.enums.SystemErrorCode;
import com.travis.monolith.system.message.api.enums.SysMessagePushType;
import com.travis.monolith.system.message.api.enums.SysMessageStatus;
import com.travis.monolith.system.message.internal.entity.SysMessage;
import com.travis.monolith.system.message.internal.mapper.SysMessageMapper;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import lombok.AllArgsConstructor;
import org.quartz.JobBuilder;
import org.quartz.JobKey;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Component;

/** 负责维护消息发布时间对应的一次性 Quartz 任务。 */
@Component
@AllArgsConstructor
public class SysMessageScheduledPushScheduler {
    private static final String GROUP = "system-message";
    private static final JobKey LEGACY_JOB_KEY = JobKey.jobKey("scheduled-message-push", GROUP);

    private final Scheduler scheduler;
    private final SysMessageMapper messageMapper;

    /** 清理旧扫描任务，并为数据库中的待发送定时消息补齐一次性任务。 */
    public void initialize() {
        try {
            scheduler.deleteJob(LEGACY_JOB_KEY);
            var messages =
                    messageMapper.selectList(
                            new LambdaQueryWrapperX<SysMessage>()
                                    .eq(SysMessage::getStatus, SysMessageStatus.PENDING.getValue())
                                    .eq(
                                            SysMessage::getPushType,
                                            SysMessagePushType.SCHEDULED.getValue())
                                    .isNotNull(SysMessage::getPublishTime));
            for (SysMessage message : messages) {
                schedule(message.getId(), message.getPublishTime());
            }
        } catch (BizException exception) {
            throw exception;
        } catch (Exception exception) {
            throw schedulerError(exception);
        }
    }

    /** 创建或重排指定消息的一次性任务。 */
    public void schedule(Long messageId, LocalDateTime publishTime) {
        try {
            JobKey jobKey = jobKey(messageId);
            Trigger trigger = buildTrigger(messageId, publishTime);
            if (scheduler.checkExists(jobKey)) {
                if (scheduler.rescheduleJob(triggerKey(messageId), trigger) == null) {
                    scheduler.scheduleJob(trigger);
                }
                return;
            }
            try {
                scheduler.scheduleJob(buildJobDetail(messageId), trigger);
            } catch (ObjectAlreadyExistsException ignored) {
                scheduler.rescheduleJob(triggerKey(messageId), trigger);
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

    private org.quartz.JobDetail buildJobDetail(Long messageId) {
        return JobBuilder.newJob(SysMessageScheduledPushJob.class)
                .withIdentity(jobKey(messageId))
                .withDescription("推送定时消息 " + messageId)
                .usingJobData(SysMessageScheduledPushJob.DATA_MESSAGE_ID, messageId)
                .build();
    }

    private Trigger buildTrigger(Long messageId, LocalDateTime publishTime) {
        return TriggerBuilder.newTrigger()
                .withIdentity(triggerKey(messageId))
                .forJob(jobKey(messageId))
                .startAt(Date.from(publishTime.atZone(ZoneId.systemDefault()).toInstant()))
                .withSchedule(
                        SimpleScheduleBuilder.simpleSchedule()
                                .withRepeatCount(0)
                                .withMisfireHandlingInstructionFireNow())
                .build();
    }

    private JobKey jobKey(Long messageId) {
        return JobKey.jobKey("scheduled-message-push-" + messageId, GROUP);
    }

    private TriggerKey triggerKey(Long messageId) {
        return TriggerKey.triggerKey("scheduled-message-push-trigger-" + messageId, GROUP);
    }

    private BizException schedulerError(Exception exception) {
        return new BizException(SystemErrorCode.MESSAGE_SCHEDULER_ERROR, exception.getMessage());
    }
}
