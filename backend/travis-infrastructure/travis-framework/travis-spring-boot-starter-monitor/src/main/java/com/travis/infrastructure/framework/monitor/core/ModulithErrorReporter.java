package com.travis.infrastructure.framework.monitor.core;

import static com.travis.infrastructure.common.monitor.error.ErrorReporter.truncate;

import com.travis.infrastructure.common.monitor.error.ErrorEvent;
import com.travis.infrastructure.common.monitor.error.ErrorReporter;
import com.travis.infrastructure.framework.event.core.TransactionalApplicationEventPublisher;
import com.travis.infrastructure.framework.jackson.core.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 通过 Spring Modulith 持久化事件发布系统异常。 */
@Slf4j
@RequiredArgsConstructor
public class ModulithErrorReporter implements ErrorReporterContributor {

    private static final int MAX_SERIALIZED_EVENT_LENGTH = 3500;

    private final TransactionalApplicationEventPublisher eventPublisher;

    @Override
    public void report(ErrorEvent event) {
        var compactEvent = compact(event);
        try {
            eventPublisher.publishEventRequiresNew(compactEvent);
        } catch (Exception exception) {
            log.error(
                    "系统错误事件发布失败，sourceType={}, sourceName={}",
                    compactEvent.sourceType(),
                    compactEvent.sourceName(),
                    exception);
        }
    }

    private ErrorEvent compact(ErrorEvent event) {
        var compactEvent =
                event.toBuilder()
                        .sourceName(
                                truncate(event.sourceName(), ErrorReporter.MAX_SOURCE_NAME_LENGTH))
                        .businessKey(
                                truncate(
                                        event.businessKey(), ErrorReporter.MAX_BUSINESS_KEY_LENGTH))
                        .requestId(truncate(event.requestId(), 100))
                        .traceId(truncate(event.traceId(), 100))
                        .requestUrl(truncate(event.requestUrl(), 300))
                        .requestMethod(truncate(event.requestMethod(), 16))
                        .requestParams(
                                truncate(event.requestParams(), ErrorReporter.MAX_CONTEXT_LENGTH))
                        .exceptionClass(truncate(event.exceptionClass(), 200))
                        .message(truncate(event.message(), ErrorReporter.MAX_MESSAGE_LENGTH))
                        .stackTrace(
                                truncate(event.stackTrace(), ErrorReporter.MAX_STACK_TRACE_LENGTH))
                        .ip(truncate(event.ip(), 64))
                        .build();
        if (serializedLength(compactEvent) <= MAX_SERIALIZED_EVENT_LENGTH) {
            return compactEvent;
        }

        compactEvent =
                compactEvent.toBuilder()
                        .sourceName(truncate(compactEvent.sourceName(), 100))
                        .businessKey(truncate(compactEvent.businessKey(), 50))
                        .requestUrl(truncate(compactEvent.requestUrl(), 200))
                        .requestParams(null)
                        .message(truncate(compactEvent.message(), 150))
                        .stackTrace(truncate(compactEvent.stackTrace(), 600))
                        .build();
        if (serializedLength(compactEvent) <= MAX_SERIALIZED_EVENT_LENGTH) {
            return compactEvent;
        }

        return ErrorEvent.builder()
                .sourceType(compactEvent.sourceType())
                .userId(compactEvent.userId())
                .requestId(truncate(compactEvent.requestId(), 64))
                .traceId(truncate(compactEvent.traceId(), 64))
                .exceptionClass(truncate(compactEvent.exceptionClass(), 100))
                .build();
    }

    private int serializedLength(ErrorEvent event) {
        return JsonUtil.toJsonString(event).length();
    }
}
