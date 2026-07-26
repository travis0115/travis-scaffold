package com.travis.monolith.system.message.internal.quartz;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 消息定时推送 Quartz 配置。 */
@Configuration(proxyBeanMethods = false)
public class SysMessageQuartzConfiguration {
    @Bean
    public ApplicationRunner sysMessageScheduledPushScheduler(
            SysMessageScheduledPushScheduler scheduler) {
        return new ApplicationRunner() {
            @Override
            public void run(@NonNull ApplicationArguments args) throws Exception {
                scheduler.initialize();
            }
        };
    }
}
