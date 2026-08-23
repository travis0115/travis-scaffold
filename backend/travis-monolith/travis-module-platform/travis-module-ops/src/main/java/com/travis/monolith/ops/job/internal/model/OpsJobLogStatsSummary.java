package com.travis.monolith.ops.job.internal.model;

/** 定时任务执行日志统计汇总。 */
public record OpsJobLogStatsSummary(
        long total,
        long success,
        long failed,
        long averageDurationMillis,
        long maxDurationMillis) {}
