package com.travis.monolith.system.log.operationlog.api.request;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.web.model.PageRequest;
import com.travis.monolith.system.common.api.enums.Status;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 操作日志分页查询请求参数
 *
 * @author travis
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysOperationLogPageReq extends PageRequest {
    /** 操作用户名（模糊匹配） */
    @Size(max = 16, message = "用户名长度不能超过16个字符")
    private String username;

    /** 操作模块（模糊匹配） */
    @Size(max = 50, message = "模块长度不能超过50个字符")
    private String module;

    /** 业务类型。 */
    @Size(max = 50, message = "业务类型长度不能超过50个字符")
    private String businessType;

    /** HTTP 请求方法。 */
    @Size(max = 10, message = "请求方式长度不能超过10个字符")
    private String requestMethod;

    /** 请求地址，支持模糊匹配。 */
    @Size(max = 500, message = "请求地址长度不能超过500个字符")
    private String requestUrl;

    /** 请求 ID。 */
    @Size(max = 100, message = "请求ID长度不能超过100个字符")
    private String requestId;

    /** 操作 IP。 */
    @Size(max = 45, message = "IP地址长度不能超过45个字符")
    private String ip;

    /** 操作状态（0-失败 1-成功） */
    @EnumValue(value = Status.class, message = "状态值错误")
    private Integer status;

    /** 操作开始时间 */
    private LocalDateTime startTime;

    /** 操作结束时间 */
    private LocalDateTime endTime;
}
