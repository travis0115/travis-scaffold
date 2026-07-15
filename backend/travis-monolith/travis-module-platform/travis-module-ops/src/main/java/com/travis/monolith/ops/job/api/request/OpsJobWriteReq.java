package com.travis.monolith.ops.job.api.request;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** 任务写入类请求的公共字段访问契约。 */
public interface OpsJobWriteReq {
    /** 获取任务名称。 */
    String getJobName();

    /** 获取任务处理器名称。 */
    String getHandlerName();

    /** 获取调度类型。 */
    String getScheduleType();

    /** 获取 CRON 表达式。 */
    String getCronExpression();

    /** 获取固定间隔毫秒数。 */
    Long getIntervalMillis();

    /** 获取单次任务执行时间。 */
    LocalDateTime getExecuteAt();

    /** 获取默认执行参数。 */
    String getParams();

    /** 获取参数结构定义。 */
    String getParamSchema();

    /** 获取调度优先级。 */
    Integer getPriority();

    /** 获取并发策略。 */
    Integer getConcurrent();

    /** 获取错过执行策略。 */
    Integer getMisfirePolicy();

    /** 获取排除日期列表。 */
    List<LocalDate> getExcludedDates();

    /** 获取排除星期列表。 */
    List<Integer> getExcludedWeekdays();

    /** 获取每日允许执行时间段的起点。 */
    LocalTime getDailyStartTime();

    /** 获取每日允许执行时间段的终点。 */
    LocalTime getDailyEndTime();

    /** 获取告警接收用户 ID 列表。 */
    List<Long> getAlertUserIds();

    /** 获取任务负责人用户 ID。 */
    Long getOwnerUserId();

    /** 获取日志保留天数。 */
    Integer getLogRetentionDays();

    /** 获取备注。 */
    String getRemark();
}
