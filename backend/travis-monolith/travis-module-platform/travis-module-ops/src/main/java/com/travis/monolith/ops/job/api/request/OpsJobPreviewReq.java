package com.travis.monolith.ops.job.api.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @Min(value = 1000, message = "执行间隔不能小于1000毫秒")
    private Long intervalMillis;

    /** 单次任务的计划执行时间。 */
    private LocalDateTime executeAt;

    /** 调度类型对应的必要配置必须存在。 */
    @AssertTrue(message = "调度参数与调度类型不匹配")
    public boolean isScheduleConfigValid() {
        if (scheduleType == null) {
            return true;
        }
        return switch (scheduleType) {
            case "CRON" -> cronExpression != null && !cronExpression.isBlank();
            case "INTERVAL" -> intervalMillis != null;
            case "ONCE" -> executeAt != null;
            default -> true;
        };
    }
}
