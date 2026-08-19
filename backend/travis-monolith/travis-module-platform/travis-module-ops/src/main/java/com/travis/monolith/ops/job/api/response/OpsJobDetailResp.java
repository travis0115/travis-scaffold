package com.travis.monolith.ops.job.api.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/** 定时任务详情响应。 */
@Data
public class OpsJobDetailResp {
    /** 任务 ID。 */
    private Long id;

    /** 乐观锁版本号。 */
    private Integer lockVersion;

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

    /** 并发策略。 */
    private Integer concurrent;

    /** 错过执行策略。 */
    private Integer misfirePolicy;

    /** 告警接收用户 ID 列表。 */
    private List<Long> alertUserIds;

    /** 任务状态。 */
    private Integer status;

    /** 是否为系统内置任务（0-否 1-是）。 */
    private Integer isBuiltin;

    /** 备注。 */
    private String remark;

    /** 下一次计划执行时间。 */
    private LocalDateTime nextFireTime;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 更新时间。 */
    private LocalDateTime updateTime;
}
