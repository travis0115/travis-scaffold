package com.travis.monolith.system.log.operationlog.api.event;

import lombok.Builder;

/** 操作日志采集事件。 */
@Builder
public record OperationLogEvent(
        Long userId,
        String description,
        String module,
        String businessType,
        String method,
        String requestUrl,
        String requestMethod,
        String requestParams,
        String responseResult,
        String requestId,
        String ip,
        String location,
        String userAgent,
        String browser,
        String os,
        long duration,
        int status,
        String errorMsg) {}
