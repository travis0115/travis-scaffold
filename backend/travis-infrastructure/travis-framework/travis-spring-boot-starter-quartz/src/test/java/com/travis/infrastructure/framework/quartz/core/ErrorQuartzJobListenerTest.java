package com.travis.infrastructure.framework.quartz.core;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.travis.infrastructure.common.monitor.error.ErrorReporter;
import com.travis.infrastructure.common.monitor.error.ErrorSource;
import org.junit.jupiter.api.Test;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

class ErrorQuartzJobListenerTest {

    @Test
    void shouldReportQuartzExecutionFailure() {
        var reporter = mock(ErrorReporter.class);
        var context = mock(JobExecutionContext.class);
        var jobDetail = mock(JobDetail.class);
        var failure = new IllegalStateException("failed");
        when(context.getJobDetail()).thenReturn(jobDetail);
        when(context.getFireInstanceId()).thenReturn("fire-1");
        doReturn(QuartzDispatchJob.class).when(jobDetail).getJobClass();
        when(jobDetail.getKey()).thenReturn(new JobKey("job-1", "ops"));

        new ErrorQuartzJobListener(reporter)
                .jobWasExecuted(context, new JobExecutionException(failure));

        verify(reporter)
                .report(
                        ErrorSource.QUARTZ,
                        QuartzDispatchJob.class.getName(),
                        "ops.job-1/fire-1",
                        failure);
    }
}
