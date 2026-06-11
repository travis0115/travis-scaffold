package com.travis.infrastructure.framework.web.core.event;

import org.springframework.context.ApplicationEvent;

/** 脱敏失败事件，由 AccessLogFilter 发布，业务模块监听后写入错误日志 */
public class DesensitizeFailureEvent extends ApplicationEvent {

    private final String requestUrl;
    private final String httpMethod;
    private final String message;
    private final String exceptionClass;
    private final String stackTrace;

    public DesensitizeFailureEvent(
            Object source,
            String requestUrl,
            String httpMethod,
            String message,
            String exceptionClass,
            String stackTrace) {
        super(source);
        this.requestUrl = requestUrl;
        this.httpMethod = httpMethod;
        this.message = message;
        this.exceptionClass = exceptionClass;
        this.stackTrace = stackTrace;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getMessage() {
        return message;
    }

    public String getExceptionClass() {
        return exceptionClass;
    }

    public String getStackTrace() {
        return stackTrace;
    }
}
