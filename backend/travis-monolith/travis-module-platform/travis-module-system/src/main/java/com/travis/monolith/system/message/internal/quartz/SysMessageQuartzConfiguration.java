package com.travis.monolith.system.message.internal.quartz;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 消息定时推送 Quartz 配置。 */
@Configuration(proxyBeanMethods = false)
public class SysMessageQuartzConfiguration {

    /** 服务启动时对账一次 */
    @Bean
    public ApplicationRunner sysMessageScheduledPushInitializer(
            SysMessageScheduledPushReconcileTask reconcileTask) {
        return _ -> reconcileTask.run();
    }
}
