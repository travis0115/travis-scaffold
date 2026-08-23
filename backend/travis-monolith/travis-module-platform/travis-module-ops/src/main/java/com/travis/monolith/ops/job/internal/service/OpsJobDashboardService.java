package com.travis.monolith.ops.job.internal.service;

import com.travis.monolith.ops.job.api.enums.OpsJobDashboardRange;
import com.travis.monolith.ops.job.api.response.OpsJobDashboardResp;

/** 任务调度看板服务。 */
public interface OpsJobDashboardService {

    /** 汇总任务调度看板数据。 */
    OpsJobDashboardResp dashboard(OpsJobDashboardRange range);
}
