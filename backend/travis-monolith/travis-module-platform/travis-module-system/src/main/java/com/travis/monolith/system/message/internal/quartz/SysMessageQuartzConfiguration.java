package com.travis.monolith.system.message.internal.quartz;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** 消息定时推送 Quartz 配置。 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
public class SysMessageQuartzConfiguration {
    @Bean
    public ApplicationRunner sysMessageScheduledPushInitializer(
            SysMessageScheduledPushScheduler scheduler,
            SysMessageScheduledPushReconcileTask reconcileTask) {
        return _ -> {
            scheduler.initialize();
            reconcileTask.run();
        };
    }
}
