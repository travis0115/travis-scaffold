package com.travis.monolith.ops.job.api.response;

import java.time.LocalDateTime;
import lombok.Data;

/** 任务执行日志分页响应。 */
@Data
public class OpsJobLogPageResp {
    /** 日志 ID。 */
    private Long id;

    /** 任务 ID。 */
    private Long jobId;

    /** 任务名称快照。 */
    private String jobName;

    /** 任务处理器名称快照。 */
    private String handlerName;

    /** 执行任务的调度器实例 ID。 */
    private String schedulerInstanceId;

    /** 实际开始时间。 */
    private LocalDateTime startTime;

    /** 实际结束时间。 */
    private LocalDateTime endTime;

    /** 执行耗时，单位毫秒。 */
    private Long durationMillis;

    /** 执行状态。 */
    private Integer status;
}
