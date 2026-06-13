package com.travis.infrastructure.framework.quartz.core;

import org.quartz.JobExecutionContext;

/** 调度执行观察器，供业务模块记录日志和统计。 */
public interface QuartzJobExecutionObserver {

    default void beforeExecution(JobExecutionContext context) {}

    default void afterSuccess(JobExecutionContext context, long durationMillis) {}

    default void afterFailure(
            JobExecutionContext context, long durationMillis, Throwable throwable) {}
}
