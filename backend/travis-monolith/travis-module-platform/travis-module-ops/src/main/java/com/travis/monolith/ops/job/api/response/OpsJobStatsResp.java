package com.travis.monolith.ops.job.api.response;

import java.time.LocalDate;
import java.util.List;

/** 基于当前保留执行日志计算的任务统计。 */
public record OpsJobStatsResp(
        /** 执行总数。 */
        long total,
        /** 成功执行数。 */
        long success,
        /** 失败执行数。 */
        long failed,
        /** 执行成功率。 */
        double successRate,
        /** 平均执行耗时，单位毫秒。 */
        long averageDurationMillis,
        /** 最大执行耗时，单位毫秒。 */
        long maxDurationMillis,
        /** 第 95 百分位执行耗时，单位毫秒。 */
        long p95DurationMillis,
        /** 当前连续失败次数。 */
        long consecutiveFailures,
        /** 按日期聚合的执行趋势。 */
        List<TrendPoint> trend) {

    /** 单日任务执行趋势点。 */
    public record TrendPoint(
            /** 统计日期。 */
            LocalDate date,
            /** 成功执行数。 */
            long success,
            /** 失败执行数。 */
            long failed) {}
}
