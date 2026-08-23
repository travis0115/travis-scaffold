package com.travis.monolith.ops.job.api.response;

import com.travis.monolith.ops.job.api.enums.OpsJobDashboardRange;
import java.time.LocalDate;
import java.util.List;

/** 任务调度看板汇总数据。 */
public record OpsJobDashboardResp(
        /** 任务总数。 */
        long totalJobs,
        /** 启用任务数。 */
        long enabledJobs,
        /** 暂停任务数。 */
        long pausedJobs,
        /** 执行指标时间范围。 */
        OpsJobDashboardRange range,
        /** 时间范围内执行总数。 */
        long executions,
        /** 时间范围内运行中执行数。 */
        long runningExecutions,
        /** 时间范围内成功执行数。 */
        long successExecutions,
        /** 时间范围内失败执行数。 */
        long failedExecutions,
        /** 时间范围内已完成执行的成功率。 */
        double successRate,
        /** 近七日执行趋势。 */
        List<TrendPoint> trend) {

    /** 每日任务执行趋势。 */
    public record TrendPoint(LocalDate date, long success, long failed) {}
}
