package com.travis.monolith.ops.job.api.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OpsJobLogBaseResp {
    private Long id;
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
    private LocalDateTime createTime;
}
