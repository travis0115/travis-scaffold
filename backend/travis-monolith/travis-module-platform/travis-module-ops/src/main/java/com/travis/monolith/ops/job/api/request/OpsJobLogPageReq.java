package com.travis.monolith.ops.job.api.request;

import com.travis.infrastructure.common.web.model.PageRequest;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 定时任务执行日志分页查询参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OpsJobLogPageReq extends PageRequest {
    /** 任务 ID。 */
    private Long jobId;

    /** 任务名称，支持模糊匹配。 */
    private String jobName;

    /** 执行状态。 */
    private Integer status;

    /** 执行开始时间范围起点。 */
    private LocalDateTime startTime;

    /** 执行开始时间范围终点。 */
    private LocalDateTime endTime;
}
