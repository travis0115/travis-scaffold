package com.travis.monolith.ops.job.internal.service;

import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.ops.job.api.request.OpsJobLogPageReq;
import com.travis.monolith.ops.job.api.response.OpsJobDashboardResp;
import com.travis.monolith.ops.job.api.response.OpsJobLogDetailResp;
import com.travis.monolith.ops.job.api.response.OpsJobLogExportResp;
import com.travis.monolith.ops.job.api.response.OpsJobLogPageResp;
import com.travis.monolith.ops.job.api.response.OpsJobStatsResp;
import com.travis.monolith.ops.job.internal.entity.OpsJobLog;
import java.util.List;

public interface OpsJobLogService {
    PageResp<OpsJobLogPageResp> page(OpsJobLogPageReq req);

    OpsJobLogDetailResp getDetail(Long id);

    List<OpsJobLogExportResp> exportLogs(OpsJobLogPageReq req);

    void clean(Long jobId);

    void cleanExpired();

    OpsJobStatsResp stats(Long jobId);

    OpsJobDashboardResp dashboard();

    void saveExecution(OpsJobLog log);

    void updateExecution(OpsJobLog log);

    void invalidateStats(Long jobId);
}
