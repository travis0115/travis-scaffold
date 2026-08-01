package com.travis.monolith.ops.job.internal.quartz;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobKey;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** 运维模块内部维护任务的 Quartz 配置。 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
public class OpsQuartzInternalJobConfiguration {

    /** 过期日志清理任务的固定标识。 */
    private static final JobKey JOB_KEY = JobKey.jobKey("log-cleanup", "ops-internal");

    /** 过期日志清理触发器的固定标识。 */
    private static final TriggerKey TRIGGER_KEY =
            TriggerKey.triggerKey("log-cleanup-trigger", "ops-internal");

    /** 应用启动后立即对账业务任务配置。 */
    @Bean
    public ApplicationRunner opsJobQuartzInitializer(OpsJobQuartzReconcileTask reconcileTask) {
        return _ -> reconcileTask.run();
    }

    /** 应用启动后注册每天凌晨三点执行的过期日志清理任务。 */
    @Bean
    public ApplicationRunner opsJobLogCleanupScheduler(Scheduler scheduler) {
        return new ApplicationRunner() {
            @Override
            public void run(ApplicationArguments args) throws Exception {
                var detail =
                        JobBuilder.newJob(OpsJobLogCleanupJob.class)
                                .withIdentity(JOB_KEY)
                                .withDescription("清理过期任务执行日志")
                                .build();
                var trigger =
                        TriggerBuilder.newTrigger()
                                .withIdentity(TRIGGER_KEY)
                                .forJob(detail)
                                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 3 * * ?"))
                                .build();
                try {
                    if (!scheduler.checkExists(JOB_KEY)) {
                        scheduler.scheduleJob(detail, trigger);
                        return;
                    }
                    scheduler.addJob(detail, true, true);
                    if (scheduler.rescheduleJob(TRIGGER_KEY, trigger) == null) {
                        scheduler.scheduleJob(trigger);
                    }
                } catch (ObjectAlreadyExistsException ignored) {
                    scheduler.addJob(detail, true, true);
                    if (scheduler.rescheduleJob(TRIGGER_KEY, trigger) == null) {
                        try {
                            scheduler.scheduleJob(trigger);
                        } catch (ObjectAlreadyExistsException concurrentRegistration) {
                            scheduler.rescheduleJob(TRIGGER_KEY, trigger);
                        }
                    }
                }
            }
        };
    }
}
