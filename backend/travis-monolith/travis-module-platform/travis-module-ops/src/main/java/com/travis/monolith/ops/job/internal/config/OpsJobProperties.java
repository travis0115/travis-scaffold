package com.travis.monolith.ops.job.internal.config;

import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/** 定时任务模块配置。 */
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "travis.ops.job")
public class OpsJobProperties {

    /** 执行日志统一保留天数。 */
    @Min(1)
    private int logRetentionDays = 30;
}
