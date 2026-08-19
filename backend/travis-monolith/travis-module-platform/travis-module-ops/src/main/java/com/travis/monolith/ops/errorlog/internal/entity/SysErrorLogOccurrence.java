package com.travis.monolith.ops.errorlog.internal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import lombok.Data;

/** 错误日志单次发生快照。 */
@Data
public class SysErrorLogOccurrence {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long errorLogId;
    private Long userId;
    private String username;
    private String requestId;
    private String traceId;
    private String requestUrl;
    private String requestMethod;
    private String controllerMethod;
    private String requestParams;
    private String message;
    private String stackTrace;
    private String ip;
    private String applicationName;
    private String applicationVersion;
    private String instanceName;
    private LocalDateTime occurredTime;
}
