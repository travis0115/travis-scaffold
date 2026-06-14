package com.travis.monolith.ops.job.api.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.Data;

@Data
public class OpsJobBaseResp {
    private Long id;
    private String jobName;
    private String handlerName;
    private Boolean handlerAvailable;
    private String scheduleType;
    private String cronExpression;
    private Long intervalMillis;
    private LocalDateTime executeAt;
    private String params;
    private String paramSchema;
    private Integer priority;
    private Integer concurrent;
    private Integer misfirePolicy;
    private List<LocalDate> excludedDates;
    private List<Integer> excludedWeekdays;
    private LocalTime dailyStartTime;
    private LocalTime dailyEndTime;
    private List<Long> alertUserIds;
    private Long ownerUserId;
    private String ownerUsername;
    private Integer logRetentionDays;
    private Integer status;
    private String remark;
    private LocalDateTime nextFireTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
