package com.travis.monolith.ops.job.api.request;

import com.travis.infrastructure.common.web.model.PageRequest;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OpsJobLogPageReq extends PageRequest {
    private Long jobId;
    private String jobName;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
