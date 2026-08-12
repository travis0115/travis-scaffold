package com.travis.monolith.ops.errorlog.api.request;

import com.travis.infrastructure.common.web.model.PageRequest;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 系统异常日志分页查询参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysErrorLogPageReq extends PageRequest {
    /** 异常类名，支持模糊匹配。 */
    @Size(max = 500, message = "异常类名长度不能超过500个字符")
    private String exceptionClass;

    /** 请求地址，支持模糊匹配。 */
    @Size(max = 500, message = "请求地址长度不能超过500个字符")
    private String requestUrl;

    /** HTTP 请求方法。 */
    @Size(max = 10, message = "请求方式长度不能超过10个字符")
    private String requestMethod;

    /** 请求 IP，支持模糊匹配。 */
    @Size(max = 50, message = "IP地址长度不能超过50个字符")
    private String ip;

    /** 异常发生时间范围起点。 */
    private LocalDateTime startTime;

    /** 异常发生时间范围终点。 */
    private LocalDateTime endTime;

    /** 起始时间不得晚于结束时间。 */
    @AssertTrue(message = "开始时间不能晚于结束时间")
    public boolean isTimeRangeValid() {
        return startTime == null || endTime == null || !startTime.isAfter(endTime);
    }
}
