package com.travis.monolith.ops.job.api.response;

import java.time.LocalDate;
import java.util.List;

public record OpsJobStatsResp(
        long total,
        long success,
        long failed,
        double successRate,
        long averageDurationMillis,
        long maxDurationMillis,
        long p95DurationMillis,
        long consecutiveFailures,
        List<TrendPoint> trend) {

    public record TrendPoint(LocalDate date, long success, long failed) {}
}
