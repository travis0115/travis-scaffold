package com.travis.monolith.system.log.errorlog.internal.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SysErrorLog {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    private String requestUrl;
    private String requestMethod;
    private String controllerMethod;
    private String exceptionClass;
    private String message;
    private String stackTrace;
    private String ip;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
