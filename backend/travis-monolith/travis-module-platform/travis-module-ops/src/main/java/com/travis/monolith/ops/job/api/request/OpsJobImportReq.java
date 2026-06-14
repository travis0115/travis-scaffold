package com.travis.monolith.ops.job.api.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.Data;

/** 任务导入请求参数 */
@Data
public class OpsJobImportReq implements OpsJobWriteReq {
    @NotBlank(message = "任务名称不能为空")
    private String jobName;

    @NotBlank(message = "任务处理器不能为空")
    private String handlerName;

    /** CRON、INTERVAL、ONCE */
    @NotBlank(message = "调度类型不能为空")
    private String scheduleType;

    private String cronExpression;
    private Long intervalMillis;
    private LocalDateTime executeAt;

    private String params;
    private String paramSchema;

    @Min(value = 1, message = "优先级不能小于 1")
    @Max(value = 10, message = "优先级不能大于 10")
    private Integer priority;

    /** 0-禁止并发，1-允许并发 */
    @NotNull(message = "并发策略不能为空")
    private Integer concurrent;

    /** 0-智能策略，1-忽略错过执行，2-立即补执行一次，3-等待下次 */
    private Integer misfirePolicy;

    private List<LocalDate> excludedDates;
    private List<Integer> excludedWeekdays;
    private LocalTime dailyStartTime;
    private LocalTime dailyEndTime;

    private List<Long> alertUserIds;
    private Long ownerUserId;

    @Min(value = 1, message = "日志保留天数不能小于 1")
    private Integer logRetentionDays;

    private String remark;
}
