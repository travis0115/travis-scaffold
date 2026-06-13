package com.travis.monolith.ops.job.internal.entity;

import com.travis.infrastructure.framework.mybatis.core.BaseEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OpsJob extends BaseEntity {
    private String jobName;
    private String handlerName;
    private String scheduleType;
    private String cronExpression;
    private Long intervalMillis;
    private LocalDateTime executeAt;
    private String params;
    private String paramSchema;
    private Integer priority;
    private Integer concurrent;
    private Integer misfirePolicy;
    private String calendarConfig;
    private String alertUserIds;
    private Long ownerUserId;
    private Integer logRetentionDays;
    private Integer status;
    private String remark;
}
