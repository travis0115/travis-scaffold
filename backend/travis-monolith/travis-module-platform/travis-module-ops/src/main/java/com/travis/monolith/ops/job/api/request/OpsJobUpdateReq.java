package com.travis.monolith.ops.job.api.request;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.monolith.ops.job.api.enums.OpsJobConcurrentPolicy;
import com.travis.monolith.ops.job.api.enums.OpsJobMisfirePolicy;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/** 定时任务更新参数。 */
@Data
public class OpsJobUpdateReq {
    /** 乐观锁版本号。 */
    @NotNull(message = "版本号不能为空")
    private Integer lockVersion;

    /** 任务名称。 */
    @NotBlank(message = "任务名称不能为空")
    @Size(max = 120, message = "任务名称长度不能超过120个字符")
    private String jobName;

    /** Spring 容器中的任务处理器名称。 */
    @NotBlank(message = "任务处理器不能为空")
    @Size(max = 120, message = "任务处理器长度不能超过120个字符")
    private String handlerName;

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

    /** 默认执行参数。 */
    private String params;

    /** 并发策略：0 禁止并发，1 允许并发。 */
    @NotNull(message = "并发策略不能为空")
    @EnumValue(value = OpsJobConcurrentPolicy.class, message = "并发策略值错误")
    private Integer concurrent;

    /** 错过执行策略：0 智能，1 忽略，2 立即补执行一次，3 等待下次。 */
    @EnumValue(value = OpsJobMisfirePolicy.class, message = "错过执行策略值错误")
    private Integer misfirePolicy;

    /** 执行失败时接收告警的用户 ID 列表。 */
    private List<@Positive(message = "告警用户ID必须为正数") Long> alertUserIds;

    /** 备注。 */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

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
