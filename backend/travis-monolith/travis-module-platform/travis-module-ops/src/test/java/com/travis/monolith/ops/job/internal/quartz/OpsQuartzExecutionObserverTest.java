package com.travis.monolith.ops.job.internal.quartz;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.framework.quartz.core.QuartzDispatchJob;
import com.travis.monolith.ops.job.api.response.OpsJobResp;
import com.travis.monolith.ops.job.internal.service.OpsJobLogService;
import com.travis.monolith.ops.job.internal.service.OpsJobService;
import com.travis.monolith.system.message.api.SysMessageApi;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

class OpsQuartzExecutionObserverTest {

    @Test
    void shouldCompleteScheduledOnceJob() {
        OpsJobService jobService = mock(OpsJobService.class);
        var observer =
                new OpsQuartzExecutionObserver(
                        jobService, mock(OpsJobLogService.class), mock(SysMessageApi.class));
        JobExecutionContext context = context(false);

        observer.afterSuccess(context, 100L);

        verify(jobService).completeOnce(1001L, "fingerprint");
    }

    @Test
    void shouldNotCompleteOnceJobForManualRun() {
        OpsJobService jobService = mock(OpsJobService.class);
        var observer =
                new OpsQuartzExecutionObserver(
                        jobService, mock(OpsJobLogService.class), mock(SysMessageApi.class));
        JobExecutionContext context = context(true);

        observer.afterSuccess(context, 100L);

        verify(jobService, never())
                .completeOnce(
                        org.mockito.ArgumentMatchers.eq(1001L), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldPublishFailureMessageWithLineBreaks() {
        OpsJobService jobService = mock(OpsJobService.class);
        OpsJobLogService logService = mock(OpsJobLogService.class);
        SysMessageApi messageApi = mock(SysMessageApi.class);
        var observer = new OpsQuartzExecutionObserver(jobService, logService, messageApi);
        JobExecutionContext context = context(true);
        when(context.get("opsJobLogId")).thenReturn(2001L);
        var job = new OpsJobResp();
        job.setJobName("测试任务");
        job.setHandlerName("<handler>");
        job.setAlertUserIds(List.of(1L));
        when(jobService.find(1001L)).thenReturn(job);

        observer.afterFailure(context, 100L, new IllegalStateException("<failed>"));

        verify(messageApi)
                .publishToUsers(
                        LoginType.ADMIN,
                        "任务执行失败：测试任务",
                        "任务处理器：&lt;handler&gt;<br>执行日志ID：2001<br>异常：&lt;failed&gt;",
                        List.of(1L));
    }

    private JobExecutionContext context(boolean manualRun) {
        JobExecutionContext context = mock(JobExecutionContext.class);
        var data = new JobDataMap();
        data.put(QuartzDispatchJob.DATA_JOB_ID, 1001L);
        data.put(QuartzDispatchJob.DATA_CONFIG_FINGERPRINT, "fingerprint");
        if (manualRun) {
            data.put(QuartzDispatchJob.DATA_MANUAL_RUN, true);
        }
        when(context.getMergedJobDataMap()).thenReturn(data);
        return context;
    }
}
