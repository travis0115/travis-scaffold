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

@Configuration(proxyBeanMethods = false)
public class OpsQuartzInternalJobConfiguration {

    private static final JobKey JOB_KEY = JobKey.jobKey("log-cleanup", "ops-internal");
    private static final TriggerKey TRIGGER_KEY =
            TriggerKey.triggerKey("log-cleanup-trigger", "ops-internal");

    @Bean
    public ApplicationRunner opsJobLogCleanupScheduler(Scheduler scheduler) {
        return new ApplicationRunner() {
            @Override
            public void run(ApplicationArguments args) throws Exception {
                if (scheduler.checkExists(JOB_KEY)) {
                    return;
                }
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
                    scheduler.scheduleJob(detail, trigger);
                } catch (ObjectAlreadyExistsException ignored) {
                    // 其他集群实例已完成注册。
                }
            }
        };
    }
}
