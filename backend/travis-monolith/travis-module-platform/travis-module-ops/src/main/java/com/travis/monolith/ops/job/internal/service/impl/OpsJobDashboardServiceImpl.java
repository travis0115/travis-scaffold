package com.travis.monolith.ops.job.internal.service.impl;

import com.travis.monolith.ops.job.api.enums.OpsJobDashboardRange;
import com.travis.monolith.ops.job.api.response.OpsJobDashboardResp;
import com.travis.monolith.ops.job.internal.mapper.OpsJobLogMapper;
import com.travis.monolith.ops.job.internal.mapper.OpsJobMapper;
import com.travis.monolith.ops.job.internal.model.OpsJobCountSummary;
import com.travis.monolith.ops.job.internal.model.OpsJobLogStatsSummary;
import com.travis.monolith.ops.job.internal.service.OpsJobDashboardService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
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
    @Cacheable(key = "#range.name() + ':' + T(java.time.LocalDate).now().toString()")
    public OpsJobDashboardResp dashboard(OpsJobDashboardRange range) {
        LocalDate today = LocalDate.now();
        OpsJobCountSummary jobs = jobMapper.selectCountSummary();
        OpsJobLogStatsSummary logs =
                jobLogMapper.selectDashboardStatsSummary(
                        range.startDate(today).atStartOfDay(), today.plusDays(1).atStartOfDay());
        long completed = logs.success() + logs.failed();
        LocalDate trendStart = today.minusDays(6);
        Map<LocalDate, OpsJobDashboardResp.TrendPoint> trendByDate =
                jobLogMapper
                        .selectDashboardTrend(
                                trendStart.atStartOfDay(), today.plusDays(1).atStartOfDay())
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        point -> point.date(),
                                        point ->
                                                new OpsJobDashboardResp.TrendPoint(
                                                        point.date(),
                                                        point.success(),
                                                        point.failed()),
                                        (left, _right) -> left,
                                        LinkedHashMap::new));
        var trend = new ArrayList<OpsJobDashboardResp.TrendPoint>(7);
        for (int offset = 0; offset < 7; offset++) {
            LocalDate date = trendStart.plusDays(offset);
            trend.add(
                    trendByDate.getOrDefault(date, new OpsJobDashboardResp.TrendPoint(date, 0, 0)));
        }
        return new OpsJobDashboardResp(
                jobs.total(),
                jobs.enabled(),
                jobs.total() - jobs.enabled(),
                range,
                logs.total(),
                logs.total() - completed,
                logs.success(),
                logs.failed(),
                completed == 0 ? 0 : logs.success() * 100.0 / completed,
                trend);
    }
}
