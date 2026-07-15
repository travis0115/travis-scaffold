package com.travis.monolith.system.message.internal.quartz;

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

/** 消息定时推送 Quartz 配置。 */
@Configuration(proxyBeanMethods = false)
public class SysMessageQuartzConfiguration {
    /** 定时消息推送任务的固定 Quartz 标识。 */
    private static final JobKey JOB_KEY = JobKey.jobKey("scheduled-message-push", "system-message");

    private static final TriggerKey TRIGGER_KEY =
            TriggerKey.triggerKey("scheduled-message-push-trigger", "system-message");

    @Bean
    public ApplicationRunner sysMessageScheduledPushScheduler(Scheduler scheduler) {
        return new ApplicationRunner() {
            @Override
            public void run(ApplicationArguments args) throws Exception {
                if (scheduler.checkExists(JOB_KEY)) {
                    return;
                }
                var detail =
                        JobBuilder.newJob(SysMessageScheduledPushJob.class)
                                .withIdentity(JOB_KEY)
                                .withDescription("推送到期的定时消息")
                                .build();
                var trigger =
                        TriggerBuilder.newTrigger()
                                .withIdentity(TRIGGER_KEY)
                                .forJob(detail)
                                .withSchedule(CronScheduleBuilder.cronSchedule("0 * * * * ?"))
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
