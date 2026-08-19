package com.travis.monolith.ops.job.api.response;

import java.time.LocalDateTime;
import lombok.Data;

/** 定时任务分页响应。 */
@Data
public class OpsJobPageResp {
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

    /** 固定间隔毫秒数。 */
    private Long intervalMillis;

    /** 任务状态。 */
    private Integer status;

    /** 是否为系统内置任务。 */
    private Integer isBuiltin;

    /** 创建人 ID。 */
    private Long createBy;

    /** 创建人用户名。 */
    private String createByUsername;

    /** 下一次计划执行时间。 */
    private LocalDateTime nextFireTime;

    /** 最近一次执行开始时间。 */
    private LocalDateTime lastExecutionTime;

    /** 最近一次执行状态。 */
    private Integer lastExecutionStatus;
}
