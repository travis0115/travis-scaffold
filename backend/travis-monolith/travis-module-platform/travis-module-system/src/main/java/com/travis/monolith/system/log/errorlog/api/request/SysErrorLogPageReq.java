package com.travis.monolith.system.log.errorlog.api.request;

import com.travis.infrastructure.common.web.model.PageRequest;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysErrorLogPageReq extends PageRequest {
    private String exceptionClass;
    private String requestUrl;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
