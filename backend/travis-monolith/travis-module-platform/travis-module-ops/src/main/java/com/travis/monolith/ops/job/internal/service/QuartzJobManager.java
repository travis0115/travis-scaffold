package com.travis.monolith.ops.job.internal.service;

import com.travis.infrastructure.framework.jackson.core.JsonUtil;
import com.travis.infrastructure.framework.quartz.core.NonConcurrentQuartzDispatchJob;
import com.travis.infrastructure.framework.quartz.core.QuartzDispatchJob;
import com.travis.monolith.ops.job.api.OpsJobErrorCode;
import com.travis.monolith.ops.job.internal.entity.OpsJob;
import com.travis.monolith.ops.job.internal.model.OpsJobCalendarConfig;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.quartz.Calendar;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.ScheduleBuilder;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.calendar.DailyCalendar;
import org.quartz.impl.calendar.HolidayCalendar;
import org.quartz.impl.calendar.WeeklyCalendar;
import org.springframework.stereotype.Component;

@Component
public class QuartzJobManager {

    private static final String GROUP = "ops-job";
    private final Scheduler scheduler;

    public QuartzJobManager(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void schedule(OpsJob job) {
        try {
            delete(job.getId());
            String calendarName = registerCalendar(job);
            JobDetail detail = buildJobDetail(job);
            Trigger trigger = buildTrigger(job, calendarName);
            scheduler.scheduleJob(detail, trigger);
            if (!Integer.valueOf(1).equals(job.getStatus())) {
                scheduler.pauseJob(jobKey(job.getId()));
            }
        } catch (Exception exception) {
            throw schedulerError(exception);
        }
    }

    public void delete(Long jobId) {
        try {
            scheduler.deleteJob(jobKey(jobId));
            String calendarName = calendarName(jobId);
            if (scheduler.getCalendar(calendarName) != null) {
                scheduler.deleteCalendar(calendarName);
            }
        } catch (Exception exception) {
            throw schedulerError(exception);
        }
    }

    public void pause(Long jobId) {
        try {
            scheduler.pauseJob(jobKey(jobId));
        } catch (Exception exception) {
            throw schedulerError(exception);
        }
    }

    public void resume(Long jobId) {
        try {
            scheduler.resumeJob(jobKey(jobId));
        } catch (Exception exception) {
            throw schedulerError(exception);
        }
    }

    public void runNow(OpsJob job, String params) {
        try {
            var data = new org.quartz.JobDataMap();
            data.put(QuartzDispatchJob.DATA_PARAMS, params == null ? job.getParams() : params);
            scheduler.triggerJob(jobKey(job.getId()), data);
        } catch (Exception exception) {
            throw schedulerError(exception);
        }
    }

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

    public List<LocalDateTime> preview(OpsJob job, int count) {
        try {
            Calendar calendar = buildCalendar(job);
            Trigger trigger = buildTrigger(job, calendar == null ? null : "preview");
            List<LocalDateTime> result = new ArrayList<>();
            Date next = trigger.getFireTimeAfter(new Date(System.currentTimeMillis() - 1000));
            while (next != null && result.size() < Math.min(Math.max(count, 1), 20)) {
                if (calendar == null || calendar.isTimeIncluded(next.getTime())) {
                    result.add(toLocalDateTime(next));
                }
                next = trigger.getFireTimeAfter(next);
            }
            return result;
        } catch (BizException exception) {
            throw exception;
        } catch (Exception exception) {
            throw schedulerError(exception);
        }
    }

    private JobDetail buildJobDetail(OpsJob job) {
        Class<? extends org.quartz.Job> jobClass =
                Integer.valueOf(1).equals(job.getConcurrent())
                        ? QuartzDispatchJob.class
                        : NonConcurrentQuartzDispatchJob.class;
        return JobBuilder.newJob(jobClass)
                .withIdentity(jobKey(job.getId()))
                .withDescription(job.getJobName())
                .usingJobData(QuartzDispatchJob.DATA_JOB_ID, job.getId())
                .usingJobData(QuartzDispatchJob.DATA_HANDLER_NAME, job.getHandlerName())
                .usingJobData(
                        QuartzDispatchJob.DATA_PARAMS,
                        job.getParams() == null ? "{}" : job.getParams())
                .build();
    }

    private Trigger buildTrigger(OpsJob job, String calendarName) {
        TriggerBuilder<Trigger> builder =
                TriggerBuilder.newTrigger()
                        .withIdentity(triggerKey(job.getId()))
                        .forJob(jobKey(job.getId()))
                        .withPriority(
                                job.getPriority() == null
                                        ? Trigger.DEFAULT_PRIORITY
                                        : job.getPriority());
        if (calendarName != null) {
            builder.modifiedByCalendar(calendarName);
        }
        ScheduleBuilder<?> schedule = buildSchedule(job);
        switch (job.getScheduleType()) {
            case "CRON" -> builder.withSchedule(schedule).startNow();
            case "INTERVAL" -> builder.withSchedule(schedule).startNow();
            case "ONCE" ->
                    builder.startAt(
                            Date.from(
                                    job.getExecuteAt().atZone(ZoneId.systemDefault()).toInstant()));
            default -> throw new BizException(OpsJobErrorCode.INVALID_SCHEDULE, "不支持的调度类型");
        }
        return builder.build();
    }

    private ScheduleBuilder<?> buildSchedule(OpsJob job) {
        int policy = job.getMisfirePolicy() == null ? 0 : job.getMisfirePolicy();
        if ("CRON".equals(job.getScheduleType())) {
            if (job.getCronExpression() == null
                    || !org.quartz.CronExpression.isValidExpression(job.getCronExpression())) {
                throw new BizException(OpsJobErrorCode.INVALID_SCHEDULE, "Cron 表达式不合法");
            }
            CronScheduleBuilder builder = CronScheduleBuilder.cronSchedule(job.getCronExpression());
            return switch (policy) {
                case 1 -> builder.withMisfireHandlingInstructionIgnoreMisfires();
                case 2 -> builder.withMisfireHandlingInstructionFireAndProceed();
                case 3 -> builder.withMisfireHandlingInstructionDoNothing();
                default -> builder;
            };
        }
        if ("INTERVAL".equals(job.getScheduleType())) {
            if (job.getIntervalMillis() == null || job.getIntervalMillis() <= 0) {
                throw new BizException(OpsJobErrorCode.INVALID_SCHEDULE, "固定间隔必须大于 0");
            }
            SimpleScheduleBuilder builder =
                    SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInMilliseconds(job.getIntervalMillis())
                            .repeatForever();
            return switch (policy) {
                case 1 -> builder.withMisfireHandlingInstructionIgnoreMisfires();
                case 2 -> builder.withMisfireHandlingInstructionFireNow();
                case 3 -> builder.withMisfireHandlingInstructionNextWithRemainingCount();
                default -> builder;
            };
        }
        if ("ONCE".equals(job.getScheduleType()) && job.getExecuteAt() == null) {
            throw new BizException(OpsJobErrorCode.INVALID_SCHEDULE, "单次任务必须指定执行时间");
        }
        return SimpleScheduleBuilder.simpleSchedule();
    }

    private String registerCalendar(OpsJob job) throws Exception {
        Calendar calendar = buildCalendar(job);
        if (calendar == null) {
            return null;
        }
        String name = calendarName(job.getId());
        scheduler.addCalendar(name, calendar, true, true);
        return name;
    }

    private Calendar buildCalendar(OpsJob job) {
        OpsJobCalendarConfig config =
                job.getCalendarConfig() == null
                        ? null
                        : JsonUtil.parseObject(job.getCalendarConfig(), OpsJobCalendarConfig.class);
        if (config == null || config.isEmpty()) {
            return null;
        }
        Calendar calendar = null;
        if (!config.excludedWeekdays().isEmpty()) {
            WeeklyCalendar weekly = new WeeklyCalendar();
            for (Integer weekday : config.excludedWeekdays()) {
                if (weekday != null && weekday >= 1 && weekday <= 7) {
                    weekly.setDayExcluded(weekday, true);
                }
            }
            calendar = weekly;
        }
        if (!config.excludedDates().isEmpty()) {
            HolidayCalendar holidays = new HolidayCalendar(calendar);
            config.excludedDates()
                    .forEach(
                            date ->
                                    holidays.addExcludedDate(
                                            Date.from(
                                                    date.atStartOfDay(ZoneId.systemDefault())
                                                            .toInstant())));
            calendar = holidays;
        }
        if (config.dailyStartTime() != null && config.dailyEndTime() != null) {
            DailyCalendar daily =
                    new DailyCalendar(
                            calendar,
                            config.dailyStartTime().toString(),
                            config.dailyEndTime().toString());
            daily.setInvertTimeRange(true);
            calendar = daily;
        }
        return calendar;
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

    private LocalDateTime toLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    private BizException schedulerError(Exception exception) {
        return new BizException(OpsJobErrorCode.SCHEDULER_ERROR, exception.getMessage());
    }
}
