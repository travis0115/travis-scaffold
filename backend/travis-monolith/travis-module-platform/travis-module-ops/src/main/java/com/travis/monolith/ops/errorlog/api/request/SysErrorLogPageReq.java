package com.travis.monolith.ops.errorlog.api.request;

import com.travis.infrastructure.common.web.model.PageRequest;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 系统异常日志分页查询参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysErrorLogPageReq extends PageRequest {
    /** 异常类名，支持模糊匹配。 */
    private String exceptionClass;

    /** 请求地址，支持模糊匹配。 */
    private String requestUrl;

    /** 异常发生时间范围起点。 */
    private LocalDateTime startTime;

    /** 异常发生时间范围终点。 */
    private LocalDateTime endTime;
}
