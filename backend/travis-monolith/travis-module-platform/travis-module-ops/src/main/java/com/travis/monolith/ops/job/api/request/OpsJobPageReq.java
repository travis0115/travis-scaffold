package com.travis.monolith.ops.job.api.request;

import com.travis.infrastructure.common.web.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OpsJobPageReq extends PageRequest {
    private String jobName;
    private String handlerName;
    private String scheduleType;
    private Integer status;
    private Long ownerUserId;
}
