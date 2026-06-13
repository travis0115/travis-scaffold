package com.travis.monolith.ops.job.internal.quartz;

import com.travis.infrastructure.framework.quartz.core.QuartzDispatchJob;
import com.travis.infrastructure.framework.quartz.core.QuartzJobExecutionObserver;
import com.travis.monolith.ops.job.internal.entity.OpsJob;
import com.travis.monolith.ops.job.internal.entity.OpsJobLog;
import com.travis.monolith.ops.job.internal.mapper.OpsJobMapper;
import com.travis.monolith.ops.job.internal.service.OpsJobLogService;
import com.travis.monolith.system.notice.api.SysNoticeApi;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpsQuartzExecutionObserver implements QuartzJobExecutionObserver {

    private final OpsJobMapper jobMapper;
    private final OpsJobLogService logService;
    private final SysNoticeApi noticeApi;
    private final Map<String, Long> executingLogs = new ConcurrentHashMap<>();

    @Override
    public void beforeExecution(JobExecutionContext context) {
        Long jobId = context.getMergedJobDataMap().getLong(QuartzDispatchJob.DATA_JOB_ID);
        OpsJob job = jobMapper.selectById(jobId);
        if (job == null) {
            return;
        }
        var executionLog = new OpsJobLog();
        executionLog.setJobId(jobId);
        executionLog.setJobName(job.getJobName());
        executionLog.setHandlerName(job.getHandlerName());
        executionLog.setFireInstanceId(context.getFireInstanceId());
        try {
            executionLog.setSchedulerInstanceId(context.getScheduler().getSchedulerInstanceId());
        } catch (Exception exception) {
            executionLog.setSchedulerInstanceId("unknown");
        }
        executionLog.setParamsSnapshot(
                context.getMergedJobDataMap().getString(QuartzDispatchJob.DATA_PARAMS));
        if (context.getScheduledFireTime() != null) {
            executionLog.setScheduledFireTime(
                    LocalDateTime.ofInstant(
                            context.getScheduledFireTime().toInstant(), ZoneId.systemDefault()));
        }
        executionLog.setStartTime(LocalDateTime.now());
        executionLog.setStatus(0);
        executionLog.setAlertStatus(0);
        logService.saveExecution(executionLog);
        executingLogs.put(context.getFireInstanceId(), executionLog.getId());
    }

    @Override
    public void afterSuccess(JobExecutionContext context, long durationMillis) {
        finish(context, durationMillis, null);
    }

    @Override
    public void afterFailure(
            JobExecutionContext context, long durationMillis, Throwable throwable) {
        finish(context, durationMillis, throwable);
    }

    private void finish(JobExecutionContext context, long durationMillis, Throwable throwable) {
        Long logId = executingLogs.remove(context.getFireInstanceId());
        if (logId == null) {
            return;
        }
        OpsJobLog executionLog = new OpsJobLog();
        executionLog.setId(logId);
        executionLog.setJobId(context.getMergedJobDataMap().getLong(QuartzDispatchJob.DATA_JOB_ID));
        executionLog.setEndTime(LocalDateTime.now());
        executionLog.setDurationMillis(durationMillis);
        executionLog.setStatus(throwable == null ? 1 : 2);
        executionLog.setResultMessage(throwable == null ? "执行成功" : "执行失败");
        if (throwable != null) {
            executionLog.setExceptionClass(throwable.getClass().getName());
            executionLog.setExceptionMessage(throwable.getMessage());
            executionLog.setStackTrace(stackTrace(throwable));
        }
        logService.updateExecution(executionLog);
        if (throwable != null) {
            publishFailure(executionLog.getJobId(), logId, throwable);
        }
    }

    private void publishFailure(Long jobId, Long logId, Throwable throwable) {
        OpsJob job = jobMapper.selectById(jobId);
        if (job == null || job.getAlertUserIds() == null || job.getAlertUserIds().isBlank()) {
            return;
        }
        try {
            List<Long> recipients =
                    Arrays.stream(job.getAlertUserIds().split(",")).map(Long::valueOf).toList();
            noticeApi.publishToUsers(
                    "任务执行失败：" + job.getJobName(),
                    "任务处理器："
                            + job.getHandlerName()
                            + "\n执行日志ID："
                            + logId
                            + "\n异常："
                            + throwable.getMessage(),
                    recipients);
            var update = new OpsJobLog();
            update.setId(logId);
            update.setJobId(jobId);
            update.setAlertStatus(1);
            logService.updateExecution(update);
        } catch (Exception exception) {
            log.warn("任务失败告警发送失败, jobId={}, logId={}", jobId, logId, exception);
        }
    }

    private String stackTrace(Throwable throwable) {
        var writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        String value = writer.toString();
        return value.length() <= 16000 ? value : value.substring(0, 16000);
    }
}
