package com.travis.monolith.ops.errorlog.internal.listener;

import com.travis.infrastructure.common.monitor.error.ErrorEvent;
import com.travis.monolith.ops.errorlog.internal.entity.SysErrorLog;
import com.travis.monolith.ops.errorlog.internal.service.SysErrorLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/** 将系统未预期异常事件持久化为错误日志。 */
@Component
@RequiredArgsConstructor
public class SystemErrorEventListener {

    private final SysErrorLogService errorLogService;

    @ApplicationModuleListener
    public void handle(ErrorEvent event) {
        var errorLog = new SysErrorLog();
        errorLog.setSourceType(event.sourceType().name());
        errorLog.setSourceName(event.sourceName());
        errorLog.setBusinessKey(event.businessKey());
        errorLog.setUserId(event.userId());
        errorLog.setRequestId(event.requestId());
        errorLog.setTraceId(event.traceId());
        errorLog.setRequestUrl(event.requestUrl());
        errorLog.setRequestMethod(event.requestMethod());
        errorLog.setControllerMethod(event.sourceName());
        errorLog.setRequestParams(event.requestParams());
        errorLog.setExceptionClass(event.exceptionClass());
        errorLog.setMessage(event.message());
        errorLog.setStackTrace(event.stackTrace());
        errorLog.setIp(event.ip());
        errorLogService.save(errorLog);
    }
}
