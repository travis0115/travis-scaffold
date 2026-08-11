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
