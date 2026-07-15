package com.travis.monolith.ops.job.api.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.Data;

/** 定时任务更新参数。 */
@Data
public class OpsJobUpdateReq implements OpsJobWriteReq {
    /** 任务名称。 */
    @NotBlank(message = "任务名称不能为空")
    private String jobName;

    /** Spring 容器中的任务处理器名称。 */
    @NotBlank(message = "任务处理器不能为空")
    private String handlerName;

    /** 调度类型：CRON、INTERVAL 或 ONCE。 */
    @NotBlank(message = "调度类型不能为空")
    private String scheduleType;

    /** CRON 调度表达式。 */
    private String cronExpression;

    /** 固定间隔调度的间隔毫秒数。 */
    private Long intervalMillis;

    /** 单次任务的计划执行时间。 */
    private LocalDateTime executeAt;

    /** 默认执行参数。 */
    private String params;

    /** 参数结构定义，用于前端生成参数表单。 */
    private String paramSchema;

    /** 调度优先级，取值 1 至 10。 */
    @Min(value = 1, message = "优先级不能小于 1")
    @Max(value = 10, message = "优先级不能大于 10")
    private Integer priority;

    /** 并发策略：0 禁止并发，1 允许并发。 */
    @NotNull(message = "并发策略不能为空")
    private Integer concurrent;

    /** 错过执行策略：0 智能，1 忽略，2 立即补执行一次，3 等待下次。 */
    private Integer misfirePolicy;

    /** 不执行任务的日期列表。 */
    private List<LocalDate> excludedDates;

    /** 不执行任务的星期列表。 */
    private List<Integer> excludedWeekdays;

    /** 每日允许执行时间段的起点。 */
    private LocalTime dailyStartTime;

    /** 每日允许执行时间段的终点。 */
    private LocalTime dailyEndTime;

    /** 执行失败时接收告警的用户 ID 列表。 */
    private List<Long> alertUserIds;

    /** 任务负责人用户 ID。 */
    private Long ownerUserId;

    /** 执行日志保留天数。 */
    @Min(value = 1, message = "日志保留天数不能小于 1")
    private Integer logRetentionDays;

    /** 备注。 */
    private String remark;
}
