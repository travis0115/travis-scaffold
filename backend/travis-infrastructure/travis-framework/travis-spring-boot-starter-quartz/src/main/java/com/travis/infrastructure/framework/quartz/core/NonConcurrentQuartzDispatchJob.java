package com.travis.infrastructure.framework.quartz.core;

import org.quartz.DisallowConcurrentExecution;

/** 同一 JobKey 不允许重叠执行的统一任务。 */
@DisallowConcurrentExecution
public class NonConcurrentQuartzDispatchJob extends QuartzDispatchJob {

    public NonConcurrentQuartzDispatchJob(
            QuartzJobHandlerRegistry registry, QuartzJobExecutionObserver observer) {
        super(registry, observer);
    }
}
