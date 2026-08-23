package com.travis.monolith.ops.job.internal.service.impl;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.monolith.ops.common.api.enums.OpsErrorCode;
import com.travis.monolith.ops.job.api.request.OpsJobLogPageReq;
import com.travis.monolith.ops.job.api.response.OpsJobLogResp;
import com.travis.monolith.ops.job.api.response.OpsJobStatsResp;
import com.travis.monolith.ops.job.internal.config.OpsJobProperties;
import com.travis.monolith.ops.job.internal.converter.OpsJobLogConverter;
import com.travis.monolith.ops.job.internal.entity.OpsJobLog;
import com.travis.monolith.ops.job.internal.mapper.OpsJobLogMapper;
import com.travis.monolith.ops.job.internal.mapper.OpsJobMapper;
import com.travis.monolith.ops.job.internal.model.OpsJobLogStatsSummary;
import com.travis.monolith.ops.job.internal.service.OpsJobLogService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 定时任务执行日志服务实现，负责日志查询、清理、统计及执行结果持久化。 */
@Service
@RequiredArgsConstructor
public class OpsJobLogServiceImpl extends ServiceImplX<OpsJobLogMapper, OpsJobLog>
        implements OpsJobLogService {

    private static final Map<String, SFunction<OpsJobLog, ?>> SORT_COLUMNS =
            Map.of(
                    "durationMillis", OpsJobLog::getDurationMillis,
                    "startTime", OpsJobLog::getStartTime);

    private final OpsJobLogConverter converter;
    private final OpsJobProperties jobProperties;
    private final OpsJobMapper jobMapper;

    /** 分页查询定时任务执行日志。 */
    @Override
    public PageResp<OpsJobLogResp> page(OpsJobLogPageReq req) {
        var wrapper = buildWrapper(req);
        wrapper.select(
                OpsJobLog::getId,
                OpsJobLog::getJobId,
                OpsJobLog::getJobName,
                OpsJobLog::getHandlerName,
                OpsJobLog::getSchedulerInstanceId,
                OpsJobLog::getStartTime,
                OpsJobLog::getEndTime,
                OpsJobLog::getDurationMillis,
                OpsJobLog::getStatus);
        Page<OpsJobLog> page =
                page(
                        new Page<>(req.getPageNum(), req.getPageSize()),
                        wrapper.orderByAllowed(
                                req.getOrderBy(),
                                req.getAsc(),
                                SORT_COLUMNS,
                                false,
                                OpsJobLog::getCreateTime));
        return PageConverter.toResp(page.convert(converter::toResp));
    }

    /** 查询指定定时任务执行日志，不存在时抛出业务异常。 */
    @Override
    public OpsJobLogResp getOrThrow(Long id) {
        OpsJobLog log = getById(id);
        if (log == null) {
            throw new BizException(OpsErrorCode.JOB_LOG_NOT_FOUND);
        }
        return converter.toResp(log);
    }

    /** 清理指定定时任务执行日志。 */
    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(
                        cacheNames = "ops:job-stats",
                        key = "#jobId",
                        condition = "#jobId != null"),
                @CacheEvict(
                        cacheNames = "ops:job-stats",
                        allEntries = true,
                        condition = "#jobId == null"),
                @CacheEvict(cacheNames = "ops:job-dashboard", key = "'summary'")
            })
    public void clean(Long jobId) {
        if (jobId == null) {
            baseMapper.deleteAllPhysically();
        } else {
            baseMapper.deletePhysicallyByJobId(jobId);
        }
    }

    /** 清理过期的定时任务执行日志。 */
    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(cacheNames = "ops:job-stats", allEntries = true),
                @CacheEvict(cacheNames = "ops:job-dashboard", key = "'summary'")
            })
    public void cleanExpired() {
        baseMapper.deleteExpiredPhysicallyAll(
                LocalDateTime.now().minusDays(jobProperties.getLogRetentionDays()));
    }

    /** 收敛因执行节点中断而遗留的运行中日志。 */
    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(cacheNames = "ops:job-stats", allEntries = true),
                @CacheEvict(cacheNames = "ops:job-dashboard", key = "'summary'")
            })
    public void markInterruptedExecutions() {
        baseMapper.markInterruptedExecutions(LocalDateTime.now());
    }

    /** 统计定时任务执行日志执行情况。 */
    @Override
    @Cacheable(cacheNames = "ops:job-stats", key = "#jobId")
    public OpsJobStatsResp stats(Long jobId) {
        if (jobMapper.selectById(jobId) == null) {
            throw new BizException(OpsErrorCode.JOB_NOT_FOUND);
        }
        OpsJobLogStatsSummary summary = baseMapper.selectStatsSummary(jobId);
        long completed = summary.success() + summary.failed();
        LocalDate firstDay = LocalDate.now().minusDays(6);
        Map<LocalDate, OpsJobStatsResp.TrendPoint> trendByDate =
                baseMapper.selectTrend(jobId, firstDay.atStartOfDay()).stream()
                        .collect(
                                Collectors.toMap(
                                        point -> point.date(),
                                        point ->
                                                new OpsJobStatsResp.TrendPoint(
                                                        point.date(),
                                                        point.success(),
                                                        point.failed()),
                                        (left, _right) -> left,
                                        LinkedHashMap::new));
        List<OpsJobStatsResp.TrendPoint> trend = new ArrayList<>(7);
        for (int offset = 0; offset < 7; offset++) {
            LocalDate date = firstDay.plusDays(offset);
            trend.add(trendByDate.getOrDefault(date, new OpsJobStatsResp.TrendPoint(date, 0, 0)));
        }
        return new OpsJobStatsResp(
                summary.total(),
                summary.success(),
                summary.failed(),
                completed == 0 ? 0 : summary.success() * 100.0 / completed,
                summary.averageDurationMillis(),
                summary.maxDurationMillis(),
                baseMapper.selectP95Duration(jobId),
                baseMapper.selectConsecutiveFailures(jobId),
                trend);
    }

    /** 保存定时任务执行日志。 */
    @Override
    @Caching(
            evict = {
                @CacheEvict(cacheNames = "ops:job-stats", key = "#log.jobId"),
                @CacheEvict(cacheNames = "ops:job-dashboard", key = "'summary'")
            })
    public void saveExecution(OpsJobLog log) {
        super.save(log);
    }

    /** 更新定时任务执行结果。 */
    @Override
    @Caching(
            evict = {
                @CacheEvict(cacheNames = "ops:job-stats", key = "#log.jobId"),
                @CacheEvict(cacheNames = "ops:job-dashboard", key = "'summary'")
            })
    public void updateExecution(OpsJobLog log) {
        updateById(log);
    }

    /** 根据查询条件构建定时任务执行日志查询条件。 */
    private LambdaQueryWrapperX<OpsJobLog> buildWrapper(OpsJobLogPageReq req) {
        return new LambdaQueryWrapperX<OpsJobLog>()
                .eqIfPresent(OpsJobLog::getJobId, req.getJobId())
                .likeIfPresent(OpsJobLog::getJobName, req.getJobName())
                .eqIfPresent(OpsJobLog::getHandlerName, req.getHandlerName())
                .eqIfPresent(OpsJobLog::getStatus, req.getStatus())
                .geIfPresent(OpsJobLog::getStartTime, req.getStartTime())
                .leIfPresent(OpsJobLog::getStartTime, req.getEndTime());
    }

    /** 批量查询各任务最近一次执行日志。 */
    @Override
    public Map<Long, OpsJobLog> latestByJobIds(Collection<Long> jobIds) {
        if (jobIds == null || jobIds.isEmpty()) {
            return Map.of();
        }
        return baseMapper.selectLatestByJobIds(jobIds).stream()
                .collect(Collectors.toMap(OpsJobLog::getJobId, log -> log));
    }
}
