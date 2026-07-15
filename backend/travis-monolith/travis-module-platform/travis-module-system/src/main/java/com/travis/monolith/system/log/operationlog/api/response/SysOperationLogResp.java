package com.travis.monolith.system.log.operationlog.api.response;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 操作日志响应对象
 *
 * @author travis
 */
@Data
public class SysOperationLogResp {

    /** 操作日志 ID。 */
    private Long id;

    /** 操作用户ID */
    private Long userId;

    /** 操作用户名 */
    private String username;

    /** 操作描述 */
    private String description;

    /** 所属业务模块 */
    private String module;

    /** 业务类型 */
    private String businessType;

    /** 请求方法全限定名 */
    private String method;

    /** 请求URL */
    private String requestUrl;

    /** HTTP 请求方法（GET/POST/PUT/DELETE） */
    private String requestMethod;

    /** 请求参数（JSON 格式） */
    private String requestParams;

    /** 响应结果（JSON 格式） */
    private String responseResult;

    /** 请求ID */
    private String requestId;

    /** 操作IP地址 */
    private String ip;

    /** 操作地点 */
    private String location;

    /** User-Agent */
    private String userAgent;

    /** 浏览器类型 */
    private String browser;

    /** 操作系统 */
    private String os;

    /** 执行耗时（毫秒） */
    private Long duration;

    /** 操作状态（0-失败 1-成功） */
    private Integer status;

    /** 错误信息（失败时记录） */
    private String errorMsg;

    /** 操作时间 */
    private LocalDateTime createTime;
}
