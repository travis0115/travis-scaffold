package com.travis.monolith.ops.job.api.response;

import java.time.LocalDateTime;
import lombok.Data;

/** 任务执行日志详情响应。 */
@Data
public class OpsJobLogDetailResp {
    /** 日志 ID。 */
    private Long id;

    /** 任务 ID。 */
    private Long jobId;

    /** 任务名称快照。 */
    private String jobName;

    /** 任务处理器名称快照。 */
    private String handlerName;

    /** Quartz 本次触发实例 ID。 */
    private String fireInstanceId;

    /** 执行任务的调度器实例 ID。 */
    private String schedulerInstanceId;

    /** 本次执行参数快照。 */
    private String paramsSnapshot;

    /** 原计划触发时间。 */
    private LocalDateTime scheduledFireTime;

    /** 实际开始时间。 */
    private LocalDateTime startTime;

    /** 实际结束时间。 */
    private LocalDateTime endTime;

    /** 执行耗时，单位毫秒。 */
    private Long durationMillis;

    /** 执行状态。 */
    private Integer status;

    /** 执行结果摘要。 */
    private String resultMessage;

    /** 异常类名。 */
    private String exceptionClass;

    /** 异常消息。 */
    private String exceptionMessage;

    /** 完整异常堆栈。 */
    private String stackTrace;

    /** 日志创建时间。 */
    private LocalDateTime createTime;
}
