package com.travis.monolith.ops.job.internal.service.impl;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.jackson.core.JsonUtil;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.redis.core.RedisUtil;
import com.travis.monolith.ops.job.api.OpsJobErrorCode;
import com.travis.monolith.ops.job.api.request.OpsJobLogPageReq;
import com.travis.monolith.ops.job.api.response.OpsJobDashboardResp;
import com.travis.monolith.ops.job.api.response.OpsJobLogBaseResp;
import com.travis.monolith.ops.job.api.response.OpsJobLogDetailResp;
import com.travis.monolith.ops.job.api.response.OpsJobLogExportResp;
import com.travis.monolith.ops.job.api.response.OpsJobLogPageResp;
import com.travis.monolith.ops.job.api.response.OpsJobStatsResp;
import com.travis.monolith.ops.job.internal.entity.OpsJob;
import com.travis.monolith.ops.job.internal.entity.OpsJobLog;
import com.travis.monolith.ops.job.internal.mapper.OpsJobLogMapper;
import com.travis.monolith.ops.job.internal.mapper.OpsJobMapper;
import com.travis.monolith.ops.job.internal.service.OpsJobLogService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpsJobLogServiceImpl extends ServiceImpl<OpsJobLogMapper, OpsJobLog>
        implements OpsJobLogService {

    private static final String STATS_KEY_PREFIX = "travis:ops:job:stats:";
    private static final String DASHBOARD_KEY = "travis:ops:job:dashboard";
    private static final long CACHE_MILLIS = TimeUnit.MINUTES.toMillis(10);
    private static final Map<String, SFunction<OpsJobLog, ?>> SORT_COLUMNS =
            Map.of(
                    "jobName", OpsJobLog::getJobName,
                    "status", OpsJobLog::getStatus,
                    "durationMillis", OpsJobLog::getDurationMillis,
                    "startTime", OpsJobLog::getStartTime,
                    "createTime", OpsJobLog::getCreateTime);

    private final OpsJobMapper jobMapper;

    @Override
    public PageResp<OpsJobLogPageResp> page(OpsJobLogPageReq req) {
        Page<OpsJobLog> page =
                page(
                        new Page<>(req.getPageNum(), req.getPageSize()),
                        buildWrapper(req)
                                .orderByAllowed(
                                        req.getOrderBy(),
                                        req.getAsc(),
                                        SORT_COLUMNS,
                                        false,
                                        OpsJobLog::getCreateTime));
        return PageConverter.toResp(page.convert(log -> toResponse(log, new OpsJobLogPageResp())));
    }

    @Override
    public OpsJobLogDetailResp getDetail(Long id) {
        OpsJobLog log = getById(id);
        if (log == null) {
            throw new BizException(OpsJobErrorCode.LOG_NOT_FOUND);
        }
        return toResponse(log, new OpsJobLogDetailResp());
    }

    @Override
    public List<OpsJobLogExportResp> exportLogs(OpsJobLogPageReq req) {
        return list(buildWrapper(req).orderByDesc(OpsJobLog::getCreateTime)).stream()
                .map(log -> toResponse(log, new OpsJobLogExportResp()))
                .toList();
    }

    @Override
    @Transactional
    public void clean(Long jobId) {
        if (jobId == null) {
            baseMapper.deleteAllPhysically();
        } else {
            baseMapper.deletePhysicallyByJobId(jobId);
        }
        invalidateStats(jobId);
    }

    @Override
    @Transactional
    public void cleanExpired() {
        List<OpsJob> jobs = jobMapper.selectList();
        for (OpsJob job : jobs) {
            int retentionDays = job.getLogRetentionDays() == null ? 30 : job.getLogRetentionDays();
            baseMapper.deleteExpiredPhysically(
                    job.getId(), LocalDateTime.now().minusDays(retentionDays));
            invalidateStats(job.getId());
        }
    }

    @Override
    public OpsJobStatsResp stats(Long jobId) {
        String key = STATS_KEY_PREFIX + jobId;
        Object cached = getCache(key);
        if (cached instanceof String value) {
            return JsonUtil.parseObject(value, OpsJobStatsResp.class);
        }
        List<OpsJobLog> logs =
                list(
                        new LambdaQueryWrapperX<OpsJobLog>()
                                .eq(OpsJobLog::getJobId, jobId)
                                .orderByAsc(OpsJobLog::getCreateTime));
        OpsJobStatsResp stats = calculateStats(logs);
        setCache(key, stats);
        return stats;
    }

    @Override
    public OpsJobDashboardResp dashboard() {
        Object cached = getCache(DASHBOARD_KEY);
        if (cached instanceof String value) {
            return JsonUtil.parseObject(value, OpsJobDashboardResp.class);
        }
        long totalJobs = jobMapper.selectCount();
        long enabledJobs =
                jobMapper.selectCount(new LambdaQueryWrapperX<OpsJob>().eq(OpsJob::getStatus, 1));
        long executions = count();
        long success = count(new LambdaQueryWrapperX<OpsJobLog>().eq(OpsJobLog::getStatus, 1));
        long failed = count(new LambdaQueryWrapperX<OpsJobLog>().eq(OpsJobLog::getStatus, 2));
        var response =
                new OpsJobDashboardResp(
                        totalJobs,
                        enabledJobs,
                        totalJobs - enabledJobs,
                        executions,
                        success,
                        failed,
                        executions == 0 ? 0 : success * 100.0 / executions);
        setCache(DASHBOARD_KEY, response);
        return response;
    }

    @Override
    public void saveExecution(OpsJobLog log) {
        super.save(log);
        invalidateStats(log.getJobId());
    }

    @Override
    public void updateExecution(OpsJobLog log) {
        updateById(log);
        invalidateStats(log.getJobId());
    }

    @Override
    public void invalidateStats(Long jobId) {
        try {
            if (jobId == null) {
                RedisUtil.deleteByPattern(STATS_KEY_PREFIX + "*");
            } else {
                RedisUtil.delete(STATS_KEY_PREFIX + jobId);
            }
            RedisUtil.delete(DASHBOARD_KEY);
        } catch (RuntimeException exception) {
            log.warn("任务统计缓存失效失败, jobId={}", jobId, exception);
        }
    }

    private Object getCache(String key) {
        try {
            return RedisUtil.get(key);
        } catch (RuntimeException exception) {
            log.warn("读取任务统计缓存失败, key={}", key, exception);
            return null;
        }
    }

    private void setCache(String key, Object value) {
        try {
            RedisUtil.set(key, JsonUtil.toJsonString(value), CACHE_MILLIS);
        } catch (RuntimeException exception) {
            log.warn("写入任务统计缓存失败, key={}", key, exception);
        }
    }

    private LambdaQueryWrapperX<OpsJobLog> buildWrapper(OpsJobLogPageReq req) {
        return new LambdaQueryWrapperX<OpsJobLog>()
                .eqIfPresent(OpsJobLog::getJobId, req.getJobId())
                .likeIfPresent(OpsJobLog::getJobName, req.getJobName())
                .eqIfPresent(OpsJobLog::getStatus, req.getStatus())
                .geIfPresent(OpsJobLog::getCreateTime, req.getStartTime())
                .leIfPresent(OpsJobLog::getCreateTime, req.getEndTime());
    }

    private OpsJobStatsResp calculateStats(List<OpsJobLog> logs) {
        long total = logs.size();
        long success =
                logs.stream().filter(log -> Integer.valueOf(1).equals(log.getStatus())).count();
        long failed =
                logs.stream().filter(log -> Integer.valueOf(2).equals(log.getStatus())).count();
        List<Long> durations =
                logs.stream()
                        .map(OpsJobLog::getDurationMillis)
                        .filter(java.util.Objects::nonNull)
                        .sorted()
                        .toList();
        long average =
                durations.isEmpty()
                        ? 0
                        : Math.round(
                                durations.stream().mapToLong(Long::longValue).average().orElse(0));
        long max = durations.isEmpty() ? 0 : durations.getLast();
        long p95 =
                durations.isEmpty()
                        ? 0
                        : durations.get(
                                Math.min(
                                        durations.size() - 1,
                                        (int) Math.ceil(durations.size() * 0.95) - 1));
        long consecutiveFailures = 0;
        for (int index = logs.size() - 1; index >= 0; index--) {
            if (!Integer.valueOf(2).equals(logs.get(index).getStatus())) {
                break;
            }
            consecutiveFailures++;
        }
        LocalDate firstDay = LocalDate.now().minusDays(6);
        Map<LocalDate, List<OpsJobLog>> grouped =
                logs.stream()
                        .filter(log -> log.getCreateTime() != null)
                        .filter(log -> !log.getCreateTime().toLocalDate().isBefore(firstDay))
                        .collect(
                                Collectors.groupingBy(
                                        log -> log.getCreateTime().toLocalDate(),
                                        LinkedHashMap::new,
                                        Collectors.toList()));
        List<OpsJobStatsResp.TrendPoint> trend = new ArrayList<>();
        for (int offset = 0; offset < 7; offset++) {
            LocalDate date = firstDay.plusDays(offset);
            List<OpsJobLog> daily = grouped.getOrDefault(date, List.of());
            trend.add(
                    new OpsJobStatsResp.TrendPoint(
                            date,
                            daily.stream()
                                    .filter(log -> Integer.valueOf(1).equals(log.getStatus()))
                                    .count(),
                            daily.stream()
                                    .filter(log -> Integer.valueOf(2).equals(log.getStatus()))
                                    .count()));
        }
        return new OpsJobStatsResp(
                total,
                success,
                failed,
                total == 0 ? 0 : success * 100.0 / total,
                average,
                max,
                p95,
                consecutiveFailures,
                trend);
    }

    private <T extends OpsJobLogBaseResp> T toResponse(OpsJobLog log, T response) {
        BeanUtils.copyProperties(log, response);
        return response;
    }
}
