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

    /** 请求地址。 */
    private String requestUrl;

    /** HTTP 请求方法。 */
    private String requestMethod;

    /** 发生异常的控制器方法。 */
    private String controllerMethod;

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
