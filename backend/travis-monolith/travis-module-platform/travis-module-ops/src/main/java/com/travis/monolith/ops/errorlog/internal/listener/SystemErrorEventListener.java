package com.travis.monolith.ops.errorlog.internal.listener;

import com.travis.infrastructure.common.monitor.error.ErrorEvent;
import com.travis.monolith.ops.errorlog.api.enums.SysErrorLogHandleStatus;
import com.travis.monolith.ops.errorlog.internal.entity.SysErrorLog;
import com.travis.monolith.ops.errorlog.internal.entity.SysErrorLogOccurrence;
import com.travis.monolith.ops.errorlog.internal.service.SysErrorLogService;
import com.travis.monolith.ops.errorlog.internal.support.ErrorLogClassifier;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/** 将系统未预期异常事件持久化为错误日志。 */
@Component
@RequiredArgsConstructor
public class SystemErrorEventListener {

    private final SysErrorLogService errorLogService;
    private final Environment environment;
    private final Optional<BuildProperties> buildProperties;

    @ApplicationModuleListener
    public void handle(ErrorEvent event) {
        var errorLog = new SysErrorLog();
        var occurredTime = LocalDateTime.now();
        errorLog.setFingerprint(
                ErrorLogClassifier.fingerprint(
                        event.sourceType(),
                        event.sourceName(),
                        event.exceptionClass(),
                        event.stackTrace()));
        errorLog.setModuleName(
                ErrorLogClassifier.moduleName(event.sourceType(), event.sourceName()));
        errorLog.setPlatformType(
                ErrorLogClassifier.platformType(event.sourceType(), event.requestUrl()));
        errorLog.setSourceType(event.sourceType().name());
        errorLog.setSourceName(event.sourceName());
        errorLog.setBusinessKey(event.businessKey());
        errorLog.setUserId(event.userId());
        errorLog.setUsername(event.username());
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
        errorLog.setStatus(SysErrorLogHandleStatus.PENDING.getValue());
        errorLog.setOccurrenceCount(1L);
        errorLog.setFirstOccurrenceTime(occurredTime);
        errorLog.setLastOccurrenceTime(occurredTime);
        errorLog.setApplicationName(environment.getProperty("spring.application.name"));
        errorLog.setApplicationVersion(
                buildProperties
                        .map(BuildProperties::getVersion)
                        .orElseGet(() -> environment.getProperty("info.app.version")));
        errorLog.setInstanceName(
                environment.getProperty(
                        "spring.application.instance-id",
                        environment.getProperty("HOSTNAME", "local")));

        var occurrence = new SysErrorLogOccurrence();
        occurrence.setUserId(event.userId());
        occurrence.setUsername(event.username());
        occurrence.setRequestId(event.requestId());
        occurrence.setTraceId(event.traceId());
        occurrence.setRequestUrl(event.requestUrl());
        occurrence.setRequestMethod(event.requestMethod());
        occurrence.setControllerMethod(event.sourceName());
        occurrence.setRequestParams(event.requestParams());
        occurrence.setMessage(event.message());
        occurrence.setStackTrace(event.stackTrace());
        occurrence.setIp(event.ip());
        occurrence.setApplicationName(errorLog.getApplicationName());
        occurrence.setApplicationVersion(errorLog.getApplicationVersion());
        occurrence.setInstanceName(errorLog.getInstanceName());
        occurrence.setOccurredTime(occurredTime);
        errorLogService.record(errorLog, occurrence);
    }
}
