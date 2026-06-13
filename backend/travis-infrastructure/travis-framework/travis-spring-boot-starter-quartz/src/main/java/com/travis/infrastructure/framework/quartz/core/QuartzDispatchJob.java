package com.travis.infrastructure.framework.quartz.core;

import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/** 根据 JobDataMap 中的处理器名称执行白名单任务。 */
@RequiredArgsConstructor
public class QuartzDispatchJob implements Job {

    public static final String DATA_JOB_ID = "jobId";
    public static final String DATA_HANDLER_NAME = "handlerName";
    public static final String DATA_PARAMS = "params";

    private final QuartzJobHandlerRegistry registry;
    private final QuartzJobExecutionObserver observer;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        long startedAt = System.currentTimeMillis();
        observer.beforeExecution(context);
        try {
            String handlerName = context.getMergedJobDataMap().getString(DATA_HANDLER_NAME);
            String params = context.getMergedJobDataMap().getString(DATA_PARAMS);
            registry.getRequired(handlerName).execute(params);
            observer.afterSuccess(context, System.currentTimeMillis() - startedAt);
        } catch (Throwable throwable) {
            observer.afterFailure(context, System.currentTimeMillis() - startedAt, throwable);
            throw new JobExecutionException(throwable, false);
        }
    }
}
