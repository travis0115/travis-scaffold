package com.travis.monolith.ops.job.internal.service.impl;

import com.travis.monolith.ops.job.api.response.OpsJobDashboardResp;
import com.travis.monolith.ops.job.internal.mapper.OpsJobLogMapper;
import com.travis.monolith.ops.job.internal.mapper.OpsJobMapper;
import com.travis.monolith.ops.job.internal.model.OpsJobCountSummary;
import com.travis.monolith.ops.job.internal.model.OpsJobLogStatsSummary;
import com.travis.monolith.ops.job.internal.service.OpsJobDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/** 汇总任务及执行日志数据，生成调度看板。 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "ops:job-dashboard")
public class OpsJobDashboardServiceImpl implements OpsJobDashboardService {

    private final OpsJobMapper jobMapper;
    private final OpsJobLogMapper jobLogMapper;

    /** 汇总任务调度看板数据。 */
    @Override
    @Cacheable(key = "'summary'")
    public OpsJobDashboardResp dashboard() {
        OpsJobCountSummary jobs = jobMapper.selectCountSummary();
        OpsJobLogStatsSummary logs = jobLogMapper.selectStatsSummary(null);
        long completed = logs.success() + logs.failed();
        return new OpsJobDashboardResp(
                jobs.total(),
                jobs.enabled(),
                jobs.total() - jobs.enabled(),
                logs.total(),
                logs.success(),
                logs.failed(),
                completed == 0 ? 0 : logs.success() * 100.0 / completed);
    }
}
