package com.travis.monolith.ops.errorlog.internal.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import lombok.Data;

/** 系统未处理异常日志实体。 */
@Data
public class SysErrorLog {
    /** 日志 ID。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 异常发生时的登录用户 ID。 */
    private Long userId;

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

    /** 异常记录时间。 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
