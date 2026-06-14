package com.travis.monolith.ops.job.api.request;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** 任务写入类请求的公共字段访问契约。 */
public interface OpsJobWriteReq {
    String getJobName();

    String getHandlerName();

    String getScheduleType();

    String getCronExpression();

    Long getIntervalMillis();

    LocalDateTime getExecuteAt();

    String getParams();

    String getParamSchema();

    Integer getPriority();

    Integer getConcurrent();

    Integer getMisfirePolicy();

    List<LocalDate> getExcludedDates();

    List<Integer> getExcludedWeekdays();

    LocalTime getDailyStartTime();

    LocalTime getDailyEndTime();

    List<Long> getAlertUserIds();

    Long getOwnerUserId();

    Integer getLogRetentionDays();

    String getRemark();
}
