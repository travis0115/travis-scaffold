package com.travis.monolith.ops.job.internal.quartz;

import com.travis.infrastructure.framework.quartz.core.QuartzDispatchJob;
import com.travis.infrastructure.framework.quartz.core.QuartzJobExecutionObserver;
import com.travis.monolith.ops.job.api.enums.OpsJobLogStatus;
import com.travis.monolith.ops.job.api.response.OpsJobDetailResp;
import com.travis.monolith.ops.job.internal.entity.OpsJobLog;
import com.travis.monolith.ops.job.internal.service.OpsJobLogService;
import com.travis.monolith.ops.job.internal.service.OpsJobService;
import com.travis.monolith.system.message.api.SysMessageApi;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

/** 记录业务任务执行过程，并在执行失败时发送站内告警。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpsQuartzExecutionObserver implements QuartzJobExecutionObserver {

    private final OpsJobService jobService;
    private final OpsJobLogService logService;
    private final SysMessageApi messageApi;

    /** Quartz 触发实例 ID 与持久化执行日志 ID 的临时映射。 */
    private final Map<String, Long> executingLogs = new ConcurrentHashMap<>();

    @Override
    public void beforeExecution(JobExecutionContext context) {
        Long jobId = context.getMergedJobDataMap().getLong(QuartzDispatchJob.DATA_JOB_ID);
        OpsJobDetailResp job = jobService.find(jobId);
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
        executionLog.setStatus(OpsJobLogStatus.RUNNING.getValue());
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

    /** 根据执行结果完成日志记录，并在失败时触发告警。 */
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

    /** 向任务配置的告警接收人发布失败通知。 */
    private void publishFailure(Long jobId, Long logId, Throwable throwable) {
        OpsJobDetailResp job = jobService.find(jobId);
        if (job == null || job.getAlertUserIds() == null || job.getAlertUserIds().isEmpty()) {
            return;
        }
        try {
            messageApi.publishToUsers(
                    "任务执行失败：" + job.getJobName(),
                    "任务处理器："
                            + job.getHandlerName()
                            + "\n执行日志ID："
                            + logId
                            + "\n异常："
                            + throwable.getMessage(),
                    job.getAlertUserIds(),
                    "OPS_JOB",
                    String.valueOf(logId));
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
