package com.travis.monolith.ops.job.internal.quartz;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 运维模块内部维护任务的 Quartz 配置。 */
@Configuration(proxyBeanMethods = false)
public class OpsQuartzInternalJobConfiguration {

    /** 过期日志清理任务的固定标识。 */
    private static final JobKey JOB_KEY = JobKey.jobKey("log-cleanup", "ops-internal");

    /** 应用启动后立即对账业务任务配置。 */
    @Bean
    public ApplicationRunner opsJobQuartzInitializer(OpsJobQuartzReconcileTask reconcileTask) {
        return _ -> reconcileTask.run();
    }

    /** 删除旧版本直接注册的日志清理任务，后续统一由 ops_job 内置任务调度。 */
    @Bean
    public ApplicationRunner legacyOpsJobLogCleanupRemover(Scheduler scheduler) {
        return _ -> scheduler.deleteJob(JOB_KEY);
    }
}
