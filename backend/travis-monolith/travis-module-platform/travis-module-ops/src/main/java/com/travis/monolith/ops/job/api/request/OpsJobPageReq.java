package com.travis.monolith.ops.job.api.request;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.web.model.PageRequest;
import com.travis.monolith.ops.job.api.enums.OpsJobStatus;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 定时任务分页查询参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OpsJobPageReq extends PageRequest {
    /** 任务名称，支持模糊匹配。 */
    @Size(max = 120, message = "任务名称长度不能超过120个字符")
    private String jobName;

    /** 任务处理器名称。 */
    @Size(max = 120, message = "任务处理器长度不能超过120个字符")
    private String handlerName;

    /** 调度类型。 */
    @Pattern(regexp = "CRON|INTERVAL|ONCE", message = "调度类型错误")
    private String scheduleType;

    /** 任务状态。 */
    @EnumValue(value = OpsJobStatus.class, message = "任务状态值错误")
    private Integer status;
}
