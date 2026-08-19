package com.travis.monolith.ops.errorlog.api.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 错误日志处理请求。 */
@Data
public class SysErrorLogHandleReq {
    /** 处理结果：1-已解决，2-已忽略。 */
    @NotNull(message = "处理结果不能为空")
    @Min(value = 1, message = "处理结果不正确")
    @Max(value = 2, message = "处理结果不正确")
    private Integer status;

    /** 处理备注。 */
    @Size(max = 500, message = "处理备注长度不能超过500个字符")
    private String remark;
}
