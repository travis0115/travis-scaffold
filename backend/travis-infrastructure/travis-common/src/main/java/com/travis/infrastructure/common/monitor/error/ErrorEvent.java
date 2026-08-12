package com.travis.infrastructure.common.monitor.error;

import lombok.Builder;

/**
 * 系统未预期异常事件。事件只携带可序列化快照，避免异步监听时依赖线程上下文。
 *
 * @param sourceType 异常来源
 * @param sourceName 来源名称
 * @param businessKey 业务定位键
 * @param userId 登录用户 ID
 * @param requestId 请求 ID
 * @param traceId 链路追踪 ID
 * @param requestUrl 请求地址
 * @param requestMethod HTTP 方法
 * @param requestParams 脱敏后的请求参数
 * @param exceptionClass 异常类名
 * @param message 异常消息
 * @param stackTrace 异常堆栈
 * @param ip 客户端 IP
 */
@Builder(toBuilder = true)
public record ErrorEvent(
        ErrorSource sourceType,
        String sourceName,
        String businessKey,
        Long userId,
        String requestId,
        String traceId,
        String requestUrl,
        String requestMethod,
        String requestParams,
        String exceptionClass,
        String message,
        String stackTrace,
        String ip) {}
