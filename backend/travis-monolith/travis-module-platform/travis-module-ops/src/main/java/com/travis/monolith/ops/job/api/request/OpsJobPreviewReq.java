package com.travis.monolith.ops.job.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;

/** 任务调度预览请求参数。 */
@Data
public class OpsJobPreviewReq {
    /** 调度类型：CRON、INTERVAL 或 ONCE。 */
    @NotBlank(message = "调度类型不能为空")
    @Pattern(regexp = "CRON|INTERVAL|ONCE", message = "调度类型错误")
    private String scheduleType;

    /** CRON 调度表达式。 */
    @Size(max = 120, message = "CRON表达式长度不能超过120个字符")
    private String cronExpression;

    /** 固定间隔调度的间隔毫秒数。 */
    @Positive(message = "执行间隔必须为正数")
    private Long intervalMillis;

    /** 单次任务的计划执行时间。 */
    private LocalDateTime executeAt;
}
