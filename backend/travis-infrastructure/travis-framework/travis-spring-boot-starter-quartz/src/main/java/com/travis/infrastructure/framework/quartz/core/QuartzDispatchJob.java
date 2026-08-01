package com.travis.infrastructure.framework.quartz.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/** 根据 JobDataMap 中的处理器名称执行白名单任务。 */
@Slf4j
@RequiredArgsConstructor
public class QuartzDispatchJob implements Job {

    /** Quartz 数据中的业务任务 ID 键。 */
    public static final String DATA_JOB_ID = "jobId";

    /** Quartz 数据中的任务处理器名称键。 */
    public static final String DATA_HANDLER_NAME = "handlerName";

    /** Quartz 数据中的任务执行参数键。 */
    public static final String DATA_PARAMS = "params";

    /** Quartz 数据中的任务配置指纹键。 */
    public static final String DATA_CONFIG_FINGERPRINT = "configFingerprint";

    /** Quartz 数据中的手动执行标记键。 */
    public static final String DATA_MANUAL_RUN = "manualRun";

    private final QuartzJobHandlerRegistry registry;
    private final QuartzJobExecutionObserver observer;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        long startedAt = System.currentTimeMillis();
        notifyBeforeExecution(context);
        try {
            String handlerName = context.getMergedJobDataMap().getString(DATA_HANDLER_NAME);
            String params = context.getMergedJobDataMap().getString(DATA_PARAMS);
            registry.getRequired(handlerName).execute(params);
            notifyAfterSuccess(context, System.currentTimeMillis() - startedAt);
        } catch (Throwable throwable) {
            notifyAfterFailure(context, System.currentTimeMillis() - startedAt, throwable);
            throw new JobExecutionException(throwable, false);
        }
    }

    private void notifyBeforeExecution(JobExecutionContext context) {
        try {
            observer.beforeExecution(context);
        } catch (Exception exception) {
            log.error(
                    "Quartz 任务执行前观察器处理失败，fireInstanceId={}",
                    context.getFireInstanceId(),
                    exception);
        }
    }

    private void notifyAfterSuccess(JobExecutionContext context, long durationMillis) {
        try {
            observer.afterSuccess(context, durationMillis);
        } catch (Exception exception) {
            log.error(
                    "Quartz 任务执行成功观察器处理失败，fireInstanceId={}",
                    context.getFireInstanceId(),
                    exception);
        }
    }

    private void notifyAfterFailure(
            JobExecutionContext context, long durationMillis, Throwable throwable) {
        try {
            observer.afterFailure(context, durationMillis, throwable);
        } catch (Exception exception) {
            throwable.addSuppressed(exception);
            log.error(
                    "Quartz 任务执行失败观察器处理失败，fireInstanceId={}",
                    context.getFireInstanceId(),
                    exception);
        }
    }
}
