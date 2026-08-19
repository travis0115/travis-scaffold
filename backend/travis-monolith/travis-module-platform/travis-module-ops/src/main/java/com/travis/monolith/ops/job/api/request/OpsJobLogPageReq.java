package com.travis.monolith.ops.job.api.request;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.web.model.PageRequest;
import com.travis.monolith.ops.job.api.enums.OpsJobLogStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 定时任务执行日志分页查询参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OpsJobLogPageReq extends PageRequest {
    /** 任务 ID。 */
    @Positive(message = "任务ID必须为正数")
    private Long jobId;

    /** 任务名称，支持模糊匹配。 */
    @Size(max = 120, message = "任务名称长度不能超过120个字符")
    private String jobName;

    /** 任务处理器名称。 */
    @Size(max = 120, message = "任务处理器长度不能超过120个字符")
    private String handlerName;

    /** 执行状态。 */
    @EnumValue(value = OpsJobLogStatus.class, message = "执行状态值错误")
    private Integer status;

    /** 执行开始时间范围起点。 */
    private LocalDateTime startTime;

    /** 执行开始时间范围终点。 */
    private LocalDateTime endTime;

    /** 起始时间不得晚于结束时间。 */
    @AssertTrue(message = "开始时间不能晚于结束时间")
    public boolean isTimeRangeValid() {
        return startTime == null || endTime == null || !startTime.isAfter(endTime);
    }
}
