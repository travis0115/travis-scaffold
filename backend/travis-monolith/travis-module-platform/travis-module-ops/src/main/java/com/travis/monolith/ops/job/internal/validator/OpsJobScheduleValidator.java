package com.travis.monolith.ops.job.internal.validator;

import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.monolith.ops.common.api.enums.OpsErrorCode;
import com.travis.monolith.ops.job.internal.entity.OpsJob;
import org.quartz.CronExpression;

/** 校验定时任务调度配置。 */
public final class OpsJobScheduleValidator {

    /** 单次预览允许返回的最大执行时间数量。 */
    public static final int MAX_PREVIEW_COUNT = 20;

    private OpsJobScheduleValidator() {}

    /** 校验任务调度类型及对应配置。 */
    public static void validate(OpsJob job) {
        if (job == null || job.getScheduleType() == null) {
            throw invalid("调度类型不能为空");
        }
        switch (job.getScheduleType()) {
            case "CRON" -> validateCron(job.getCronExpression());
            case "INTERVAL" -> validateInterval(job.getIntervalMillis());
            case "ONCE" -> validateOnce(job);
            default -> throw invalid("不支持的调度类型");
        }
    }

    private static void validateCron(String cronExpression) {
        if (cronExpression == null || !CronExpression.isValidExpression(cronExpression)) {
            throw invalid("Cron 表达式不合法");
        }
    }

    private static void validateInterval(Long intervalMillis) {
        if (intervalMillis == null || intervalMillis < 1000) {
            throw invalid("固定间隔不能小于 1000 毫秒");
        }
    }

    private static void validateOnce(OpsJob job) {
        if (job.getExecuteAt() == null) {
            throw invalid("单次任务必须指定执行时间");
        }
    }

    private static BizException invalid(String message) {
        return new BizException(OpsErrorCode.INVALID_SCHEDULE, message);
    }
}
