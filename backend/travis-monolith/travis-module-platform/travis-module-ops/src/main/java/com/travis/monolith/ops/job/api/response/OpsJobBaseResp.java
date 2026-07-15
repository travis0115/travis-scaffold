package com.travis.monolith.ops.job.api.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.Data;

/** 定时任务基础响应。 */
@Data
public class OpsJobBaseResp {
    /** 任务 ID。 */
    private Long id;

    /** 任务名称。 */
    private String jobName;

    /** 任务处理器名称。 */
    private String handlerName;

    /** 当前任务处理器是否可用。 */
    private Boolean handlerAvailable;

    /** 调度类型。 */
    private String scheduleType;

    /** CRON 调度表达式。 */
    private String cronExpression;

    /** 固定间隔调度的间隔毫秒数。 */
    private Long intervalMillis;

    /** 单次任务的计划执行时间。 */
    private LocalDateTime executeAt;

    /** 默认执行参数。 */
    private String params;

    /** 参数结构定义。 */
    private String paramSchema;

    /** 调度优先级。 */
    private Integer priority;

    /** 并发策略。 */
    private Integer concurrent;

    /** 错过执行策略。 */
    private Integer misfirePolicy;

    /** 不执行任务的日期列表。 */
    private List<LocalDate> excludedDates;

    /** 不执行任务的星期列表。 */
    private List<Integer> excludedWeekdays;

    /** 每日允许执行时间段的起点。 */
    private LocalTime dailyStartTime;

    /** 每日允许执行时间段的终点。 */
    private LocalTime dailyEndTime;

    /** 告警接收用户 ID 列表。 */
    private List<Long> alertUserIds;

    /** 任务负责人用户 ID。 */
    private Long ownerUserId;

    /** 任务负责人用户名。 */
    private String ownerUsername;

    /** 执行日志保留天数。 */
    private Integer logRetentionDays;

    /** 任务状态。 */
    private Integer status;

    /** 备注。 */
    private String remark;

    /** 下一次计划执行时间。 */
    private LocalDateTime nextFireTime;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 更新时间。 */
    private LocalDateTime updateTime;
}
