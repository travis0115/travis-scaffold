package com.travis.monolith.ops.job.api.request;

import com.travis.infrastructure.common.web.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 定时任务分页查询参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OpsJobPageReq extends PageRequest {
    /** 任务名称，支持模糊匹配。 */
    private String jobName;

    /** 任务处理器名称。 */
    private String handlerName;

    /** 调度类型。 */
    private String scheduleType;

    /** 任务状态。 */
    private Integer status;

    /** 负责人用户 ID。 */
    private Long ownerUserId;
}
