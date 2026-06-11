package com.travis.monolith.system.log.operationlog.api.event;

/** 操作日志采集事件。 */
public record OperationLogEvent(
        Long userId,
        String description,
        String module,
        String method,
        String requestUrl,
        String requestMethod,
        String requestParams,
        String responseResult,
        String ip,
        long duration,
        int status,
        String errorMsg) {}
