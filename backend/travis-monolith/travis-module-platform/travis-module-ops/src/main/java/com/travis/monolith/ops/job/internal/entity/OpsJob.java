package com.travis.monolith.ops.job.internal.entity;

import com.baomidou.mybatisplus.annotation.Version;
import com.travis.infrastructure.framework.mybatis.core.BaseEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 定时任务实体。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OpsJob extends BaseEntity {
    /** 乐观锁版本号。 */
    @Version private Integer lockVersion;

    /** 任务名称。 */
    private String jobName;

    /** Spring 容器中的任务处理器名称。 */
    private String handlerName;

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

    /** 序列化后的告警接收用户 ID 列表。 */
    private String alertUserIds;

    /** 任务状态。 */
    private Integer status;

    /** 是否为系统内置任务（0-否 1-是）。 */
    private Integer isBuiltin;

    /** 备注。 */
    private String remark;
}
