package com.travis.monolith.ops.job.api.response;

public record OpsJobDashboardResp(
        long totalJobs,
        long enabledJobs,
        long pausedJobs,
        long executions,
        long successExecutions,
        long failedExecutions,
        double successRate) {}
