package com.travis.monolith.ops.job.internal.service;

import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.framework.quartz.core.NonConcurrentQuartzDispatchJob;
import com.travis.infrastructure.framework.quartz.core.QuartzDispatchJob;
import com.travis.infrastructure.framework.redis.core.annotation.DistributedLock;
import com.travis.infrastructure.framework.redis.core.annotation.DistributedLockNamespace;
import com.travis.monolith.ops.job.api.OpsJobErrorCode;
import com.travis.monolith.ops.job.api.enums.OpsJobConcurrentPolicy;
import com.travis.monolith.ops.job.api.enums.OpsJobMisfirePolicy;
import com.travis.monolith.ops.job.api.enums.OpsJobStatus;
import com.travis.monolith.ops.job.internal.entity.OpsJob;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Component;

/** 负责将业务任务配置同步到 Quartz 调度器。 */
@Component
@AllArgsConstructor
@DistributedLockNamespace(QuartzJobManager.LOCK_NAMESPACE)
public class QuartzJobManager {

    /** 业务任务在 Quartz 中使用的统一分组。 */
    private static final String GROUP = "ops-job";

    public static final String LOCK_NAMESPACE = "ops-job";
    public static final String RECONCILE_LOCK_KEY = "quartz-reconcile";
    private static final String RECONCILE_LOCK_KEY_SPEL = "'" + RECONCILE_LOCK_KEY + "'";
    private static final long LOCK_WAIT_SECONDS = 30;

    private final Scheduler scheduler;

    /** 按业务配置同步任务；停用任务只保留不可自动触发的持久 Job。 */
    @DistributedLock(
            key = RECONCILE_LOCK_KEY_SPEL,
            waitTime = LOCK_WAIT_SECONDS,
            timeUnit = TimeUnit.SECONDS)
    public void schedule(OpsJob job) {
        try {
            String fingerprint = configFingerprint(job);
            JobDetail detail = buildJobDetail(job, fingerprint);
            if (!OpsJobStatus.ENABLED.getValue().equals(job.getStatus())) {
                scheduler.unscheduleJob(triggerKey(job.getId()));
                scheduler.addJob(detail, true);
                deleteCalendar(job.getId());
                return;
            }
            Trigger trigger = buildTrigger(job, fingerprint);
            scheduler.addJob(detail, true);
            if (scheduler.rescheduleJob(triggerKey(job.getId()), trigger) == null) {
                scheduler.scheduleJob(trigger);
            }
            deleteCalendar(job.getId());
        } catch (Exception exception) {
            throw schedulerError(exception);
        }
    }

    /** 删除任务及其关联日历。 */
    @DistributedLock(
            key = RECONCILE_LOCK_KEY_SPEL,
            waitTime = LOCK_WAIT_SECONDS,
            timeUnit = TimeUnit.SECONDS)
    public void delete(Long jobId) {
        try {
            scheduler.deleteJob(jobKey(jobId));
            deleteCalendar(jobId);
        } catch (Exception exception) {
            throw schedulerError(exception);
        }
    }

    /** 对账业务任务与 Quartz 状态，并清理不存在的孤立任务。 */
    @DistributedLock(
            key = RECONCILE_LOCK_KEY_SPEL,
            waitTime = LOCK_WAIT_SECONDS,
            timeUnit = TimeUnit.SECONDS)
    public void reconcile(List<OpsJob> jobs) {
        try {
            Set<JobKey> expectedJobKeys = new HashSet<>();
            for (OpsJob job : jobs) {
                expectedJobKeys.add(jobKey(job.getId()));
                if (!isSynchronized(job)) {
                    schedule(job);
                }
            }
            for (JobKey existingKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(GROUP))) {
                if (!expectedJobKeys.contains(existingKey)) {
                    delete(parseJobId(existingKey));
                }
            }
        } catch (Exception exception) {
            throw schedulerError(exception);
        }
    }

    /** 判断任务配置及触发器是否已经与业务数据一致。 */
    public boolean isSynchronized(OpsJob job) {
        try {
            JobDetail detail = scheduler.getJobDetail(jobKey(job.getId()));
            if (detail == null
                    || !Objects.equals(
                            configFingerprint(job),
                            detail.getJobDataMap()
                                    .getString(QuartzDispatchJob.DATA_CONFIG_FINGERPRINT))) {
                return false;
            }
            Trigger trigger = scheduler.getTrigger(triggerKey(job.getId()));
            if (!OpsJobStatus.ENABLED.getValue().equals(job.getStatus())) {
                return trigger == null && !calendarExists(job.getId());
            }
            if (calendarExists(job.getId())) {
                return false;
            }
            return trigger != null
                    && Objects.equals(
                            configFingerprint(job),
                            trigger.getJobDataMap()
                                    .getString(QuartzDispatchJob.DATA_CONFIG_FINGERPRINT));
        } catch (Exception exception) {
            throw schedulerError(exception);
        }
    }

    /** 使用指定参数立即触发一次任务；参数为空时使用任务默认参数。 */
    @DistributedLock(
            key = RECONCILE_LOCK_KEY_SPEL,
            waitTime = LOCK_WAIT_SECONDS,
            timeUnit = TimeUnit.SECONDS)
    public void runNow(OpsJob job, String params) {
        try {
            scheduler.addJob(buildJobDetail(job, configFingerprint(job)), true);
            var data = new org.quartz.JobDataMap();
            data.put(QuartzDispatchJob.DATA_PARAMS, params == null ? job.getParams() : params);
            data.put(QuartzDispatchJob.DATA_MANUAL_RUN, true);
            scheduler.triggerJob(jobKey(job.getId()), data);
        } catch (Exception exception) {
            throw schedulerError(exception);
        }
    }

    /** 查询任务下一次计划触发时间。 */
    public LocalDateTime nextFireTime(Long jobId) {
        try {
            Trigger trigger = scheduler.getTrigger(triggerKey(jobId));
            return trigger == null || trigger.getNextFireTime() == null
                    ? null
                    : toLocalDateTime(trigger.getNextFireTime());
        } catch (Exception exception) {
            throw schedulerError(exception);
        }
    }

    /** 在不注册任务的情况下预览后续计划触发时间。 */
    public List<LocalDateTime> preview(OpsJob job, int count) {
        try {
            Trigger trigger = buildTrigger(job, configFingerprint(job));
            List<LocalDateTime> result = new ArrayList<>();
            Date next = trigger.getFireTimeAfter(new Date(System.currentTimeMillis() - 1000));
            while (next != null && result.size() < Math.min(Math.max(count, 1), 20)) {
                result.add(toLocalDateTime(next));
                next = trigger.getFireTimeAfter(next);
            }
            return result;
        } catch (BizException exception) {
            throw exception;
        } catch (Exception exception) {
            throw schedulerError(exception);
        }
    }

    private JobDetail buildJobDetail(OpsJob job, String fingerprint) {
        Class<? extends org.quartz.Job> jobClass =
                OpsJobConcurrentPolicy.ALLOW.getValue().equals(job.getConcurrent())
                        ? QuartzDispatchJob.class
                        : NonConcurrentQuartzDispatchJob.class;
        return JobBuilder.newJob(jobClass)
                .withIdentity(jobKey(job.getId()))
                .withDescription(job.getJobName())
                .storeDurably()
                .usingJobData(QuartzDispatchJob.DATA_JOB_ID, job.getId())
                .usingJobData(QuartzDispatchJob.DATA_HANDLER_NAME, job.getHandlerName())
                .usingJobData(
                        QuartzDispatchJob.DATA_PARAMS,
                        job.getParams() == null ? "{}" : job.getParams())
                .usingJobData(QuartzDispatchJob.DATA_CONFIG_FINGERPRINT, fingerprint)
                .build();
    }

    private Trigger buildTrigger(OpsJob job, String fingerprint) {
        TriggerBuilder<Trigger> builder =
                TriggerBuilder.newTrigger()
                        .withIdentity(triggerKey(job.getId()))
                        .forJob(jobKey(job.getId()))
                        .usingJobData(QuartzDispatchJob.DATA_CONFIG_FINGERPRINT, fingerprint);
        ScheduleBuilder<?> schedule = buildSchedule(job);
        switch (job.getScheduleType()) {
            case "CRON" -> builder.withSchedule(schedule).startNow();
            case "INTERVAL" -> builder.withSchedule(schedule).startNow();
            case "ONCE" ->
                    builder.withSchedule(schedule)
                            .startAt(
                                    Date.from(
                                            job.getExecuteAt()
                                                    .atZone(ZoneId.systemDefault())
                                                    .toInstant()));
            default -> throw new BizException(OpsJobErrorCode.INVALID_SCHEDULE, "不支持的调度类型");
        }
        return builder.build();
    }

    private ScheduleBuilder<?> buildSchedule(OpsJob job) {
        int policy =
                job.getMisfirePolicy() == null
                        ? OpsJobMisfirePolicy.SMART.getValue()
                        : job.getMisfirePolicy();
        if ("CRON".equals(job.getScheduleType())) {
            if (job.getCronExpression() == null
                    || !org.quartz.CronExpression.isValidExpression(job.getCronExpression())) {
                throw new BizException(OpsJobErrorCode.INVALID_SCHEDULE, "Cron 表达式不合法");
            }
            CronScheduleBuilder builder = CronScheduleBuilder.cronSchedule(job.getCronExpression());
            if (OpsJobMisfirePolicy.IGNORE.getValue().equals(policy)) {
                return builder.withMisfireHandlingInstructionIgnoreMisfires();
            }
            if (OpsJobMisfirePolicy.FIRE_NOW.getValue().equals(policy)) {
                return builder.withMisfireHandlingInstructionFireAndProceed();
            }
            return OpsJobMisfirePolicy.NEXT_TIME.getValue().equals(policy)
                    ? builder.withMisfireHandlingInstructionDoNothing()
                    : builder;
        }
        if ("INTERVAL".equals(job.getScheduleType())) {
            if (job.getIntervalMillis() == null || job.getIntervalMillis() <= 0) {
                throw new BizException(OpsJobErrorCode.INVALID_SCHEDULE, "固定间隔必须大于 0");
            }
            SimpleScheduleBuilder builder =
                    SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInMilliseconds(job.getIntervalMillis())
                            .repeatForever();
            if (OpsJobMisfirePolicy.IGNORE.getValue().equals(policy)) {
                return builder.withMisfireHandlingInstructionIgnoreMisfires();
            }
            if (OpsJobMisfirePolicy.FIRE_NOW.getValue().equals(policy)) {
                return builder.withMisfireHandlingInstructionFireNow();
            }
            return OpsJobMisfirePolicy.NEXT_TIME.getValue().equals(policy)
                    ? builder.withMisfireHandlingInstructionNextWithRemainingCount()
                    : builder;
        }
        if ("ONCE".equals(job.getScheduleType())) {
            if (job.getExecuteAt() == null) {
                throw new BizException(OpsJobErrorCode.INVALID_SCHEDULE, "单次任务必须指定执行时间");
            }
            SimpleScheduleBuilder builder =
                    SimpleScheduleBuilder.simpleSchedule().withRepeatCount(0);
            if (OpsJobMisfirePolicy.IGNORE.getValue().equals(policy)) {
                return builder.withMisfireHandlingInstructionIgnoreMisfires();
            }
            if (OpsJobMisfirePolicy.FIRE_NOW.getValue().equals(policy)) {
                return builder.withMisfireHandlingInstructionFireNow();
            }
            return OpsJobMisfirePolicy.NEXT_TIME.getValue().equals(policy)
                    ? builder.withMisfireHandlingInstructionNextWithRemainingCount()
                    : builder;
        }
        return SimpleScheduleBuilder.simpleSchedule();
    }

    private JobKey jobKey(Long jobId) {
        return JobKey.jobKey("job-" + jobId, GROUP);
    }

    private TriggerKey triggerKey(Long jobId) {
        return TriggerKey.triggerKey("trigger-" + jobId, GROUP);
    }

    private String calendarName(Long jobId) {
        return "ops-job-calendar-" + jobId;
    }

    private void deleteCalendar(Long jobId) throws SchedulerException {
        String calendarName = calendarName(jobId);
        if (calendarExists(jobId)) {
            scheduler.deleteCalendar(calendarName);
        }
    }

    /** 通过名称列表判断日历是否存在，避免 Quartz JDBC 查询不存在的日历时输出误导性警告。 */
    private boolean calendarExists(Long jobId) throws SchedulerException {
        return scheduler.getCalendarNames().contains(calendarName(jobId));
    }

    private Long parseJobId(JobKey jobKey) {
        return Long.valueOf(jobKey.getName().substring("job-".length()));
    }

    /** 计算会影响 Quartz Job 或 Trigger 的业务配置指纹。 */
    public String configFingerprint(OpsJob job) {
        return Integer.toHexString(
                Objects.hash(
                        job.getJobName(),
                        job.getHandlerName(),
                        job.getScheduleType(),
                        job.getCronExpression(),
                        job.getIntervalMillis(),
                        job.getExecuteAt(),
                        job.getParams(),
                        job.getConcurrent(),
                        job.getMisfirePolicy(),
                        job.getStatus()));
    }

    private LocalDateTime toLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    private BizException schedulerError(Exception exception) {
        if (exception instanceof BizException bizException) {
            return bizException;
        }
        return new BizException(OpsJobErrorCode.SCHEDULER_ERROR, exception, exception.getMessage());
    }
}
