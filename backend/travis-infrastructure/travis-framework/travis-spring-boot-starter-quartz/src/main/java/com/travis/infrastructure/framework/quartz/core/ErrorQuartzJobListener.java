package com.travis.infrastructure.framework.quartz.core;

import com.travis.infrastructure.common.monitor.error.ErrorReporter;
import com.travis.infrastructure.common.monitor.error.ErrorSource;
import lombok.RequiredArgsConstructor;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.listeners.JobListenerSupport;

/** 在 Quartz 全局执行边界上报未预期异常。 */
@RequiredArgsConstructor
public class ErrorQuartzJobListener extends JobListenerSupport {

    private final ErrorReporter errorReporter;

    @Override
    public String getName() {
        return "errorQuartzJobListener";
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        if (jobException == null) {
            return;
        }
        var throwable = jobException.getCause() == null ? jobException : jobException.getCause();
        errorReporter.report(
                ErrorSource.QUARTZ,
                context.getJobDetail().getJobClass().getName(),
                context.getJobDetail().getKey() + "/" + context.getFireInstanceId(),
                throwable);
    }
}
