package com.travis.monolith.ops.job.internal.entity;

import com.travis.infrastructure.framework.mybatis.core.BaseEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OpsJobLog extends BaseEntity {
    private Long jobId;
    private String jobName;
    private String handlerName;
    private String fireInstanceId;
    private String schedulerInstanceId;
    private String paramsSnapshot;
    private LocalDateTime scheduledFireTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMillis;
    private Integer status;
    private String resultMessage;
    private String exceptionClass;
    private String exceptionMessage;
    private String stackTrace;
    private Integer alertStatus;
}
