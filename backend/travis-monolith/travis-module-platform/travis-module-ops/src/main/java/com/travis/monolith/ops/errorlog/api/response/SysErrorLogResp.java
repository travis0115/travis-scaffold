package com.travis.monolith.ops.errorlog.api.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/** 系统异常日志响应。 */
@Data
public class SysErrorLogResp {
    /** 日志 ID。 */
    private Long id;

    /** 异常发生时的登录用户 ID。 */
    private Long userId;

    /** 用户名。 */
    private String username;

    /** 异常指纹。 */
    private String fingerprint;

    /** 模块名称。 */
    private String moduleName;

    /** 平台类型。 */
    private String platformType;

    /** 异常来源类型。 */
    private String sourceType;

    /** 异常来源名称。 */
    private String sourceName;

    /** 业务定位键。 */
    private String businessKey;

    /** 请求 ID。 */
    private String requestId;

    /** 链路追踪 ID。 */
    private String traceId;

    /** 请求地址。 */
    private String requestUrl;

    /** HTTP 请求方法。 */
    private String requestMethod;

    /** 发生异常的控制器方法。 */
    private String controllerMethod;

    /** 脱敏后的请求参数。 */
    private String requestParams;

    /** 异常类名。 */
    private String exceptionClass;

    /** 异常消息。 */
    private String message;

    /** 完整异常堆栈。 */
    private String stackTrace;

    /** 客户端 IP。 */
    private String ip;

    /** 处理状态。 */
    private Integer status;

    /** 发生次数。 */
    private Long occurrenceCount;

    /** 首次发生时间。 */
    private LocalDateTime firstOccurrenceTime;

    /** 最后发生时间。 */
    private LocalDateTime lastOccurrenceTime;

    /** 处理人 ID。 */
    private Long handledBy;

    /** 处理人用户名。 */
    private String handledByUsername;

    /** 处理时间。 */
    private LocalDateTime handledTime;

    /** 处理备注。 */
    private String handleRemark;

    /** 应用名称。 */
    private String applicationName;

    /** 应用版本。 */
    private String applicationVersion;

    /** 实例名称。 */
    private String instanceName;

    /** 异常记录时间。 */
    private LocalDateTime createTime;

    /** 最近发生明细，按发生时间倒序。 */
    private List<SysErrorLogOccurrenceResp> occurrences;
}
