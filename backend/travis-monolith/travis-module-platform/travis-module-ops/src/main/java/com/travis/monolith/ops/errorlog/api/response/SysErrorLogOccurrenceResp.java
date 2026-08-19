package com.travis.monolith.ops.errorlog.api.response;

import java.time.LocalDateTime;
import lombok.Data;

/** 错误日志单次发生明细。 */
@Data
public class SysErrorLogOccurrenceResp {
    private Long id;
    private Long userId;
    private String username;
    private String requestId;
    private String traceId;
    private String requestUrl;
    private String requestMethod;
    private String controllerMethod;
    private String requestParams;
    private String message;
    private String stackTrace;
    private String ip;
    private String applicationName;
    private String applicationVersion;
    private String instanceName;
    private LocalDateTime occurredTime;
}
