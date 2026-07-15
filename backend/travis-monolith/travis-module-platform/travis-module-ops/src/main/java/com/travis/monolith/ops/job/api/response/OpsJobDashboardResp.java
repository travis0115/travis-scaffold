package com.travis.monolith.ops.job.api.response;

/** 任务调度看板汇总数据。 */
public record OpsJobDashboardResp(
        /** 任务总数。 */
        long totalJobs,
        /** 启用任务数。 */
        long enabledJobs,
        /** 暂停任务数。 */
        long pausedJobs,
        /** 执行总数。 */
        long executions,
        /** 成功执行数。 */
        long successExecutions,
        /** 失败执行数。 */
        long failedExecutions,
        /** 执行成功率。 */
        double successRate) {}
