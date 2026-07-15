package com.travis.monolith.system.log.operationlog.api.event;

import lombok.Builder;

/**
 * 操作日志采集事件。
 *
 * @param userId 操作用户 ID
 * @param description 操作描述
 * @param module 业务模块
 * @param businessType 业务类型
 * @param method 请求方法全限定名
 * @param requestUrl 请求地址
 * @param requestMethod HTTP 请求方法
 * @param requestParams 请求参数
 * @param responseResult 响应结果
 * @param requestId 请求 ID
 * @param ip 客户端 IP
 * @param location 客户端归属地
 * @param userAgent User-Agent
 * @param browser 浏览器类型
 * @param os 操作系统
 * @param duration 执行耗时，单位毫秒
 * @param status 操作状态
 * @param errorMsg 失败时的错误信息
 */
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
