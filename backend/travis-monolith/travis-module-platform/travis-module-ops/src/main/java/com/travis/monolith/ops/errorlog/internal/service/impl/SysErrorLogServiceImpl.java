package com.travis.monolith.ops.errorlog.internal.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.infrastructure.framework.satoken.core.StpKit;
import com.travis.monolith.ops.common.api.enums.OpsErrorCode;
import com.travis.monolith.ops.errorlog.api.enums.SysErrorLogHandleStatus;
import com.travis.monolith.ops.errorlog.api.request.SysErrorLogHandleReq;
import com.travis.monolith.ops.errorlog.api.request.SysErrorLogPageReq;
import com.travis.monolith.ops.errorlog.api.response.SysErrorLogOccurrenceResp;
import com.travis.monolith.ops.errorlog.api.response.SysErrorLogResp;
import com.travis.monolith.ops.errorlog.internal.converter.SysErrorLogConverter;
import com.travis.monolith.ops.errorlog.internal.entity.SysErrorLog;
import com.travis.monolith.ops.errorlog.internal.entity.SysErrorLogOccurrence;
import com.travis.monolith.ops.errorlog.internal.mapper.SysErrorLogMapper;
import com.travis.monolith.ops.errorlog.internal.mapper.SysErrorLogOccurrenceMapper;
import com.travis.monolith.ops.errorlog.internal.service.SysErrorLogService;
import com.travis.monolith.system.user.api.SysUserApi;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 系统异常日志服务实现，负责异常聚合、处理和查询。 */
@Service
@RequiredArgsConstructor
public class SysErrorLogServiceImpl extends ServiceImplX<SysErrorLogMapper, SysErrorLog>
        implements SysErrorLogService {
    private final SysErrorLogOccurrenceMapper occurrenceMapper;
    private final SysUserApi userApi;
    private final SysErrorLogConverter converter;

    @Override
    public PageResp<SysErrorLogResp> page(SysErrorLogPageReq req) {
        var requestMethod =
                Optional.ofNullable(req.getRequestMethod())
                        .map(String::trim)
                        .map(value -> value.toUpperCase(Locale.ROOT))
                        .orElse(null);
        var platformType =
                Optional.ofNullable(req.getPlatformType())
                        .map(String::trim)
                        .map(value -> value.toUpperCase(Locale.ROOT))
                        .orElse(null);
        var wrapper =
                new LambdaQueryWrapperX<SysErrorLog>()
                        .eqIfPresent(SysErrorLog::getStatus, req.getStatus())
                        .likeIfPresent(SysErrorLog::getModuleName, req.getModuleName())
                        .eqIfPresent(SysErrorLog::getPlatformType, platformType)
                        .likeIfPresent(SysErrorLog::getRequestId, req.getRequestId())
                        .likeIfPresent(SysErrorLog::getExceptionClass, req.getExceptionClass())
                        .likeIfPresent(SysErrorLog::getRequestUrl, req.getRequestUrl())
                        .eqIfPresent(SysErrorLog::getRequestMethod, requestMethod)
                        .likeIfPresent(SysErrorLog::getIp, req.getIp())
                        .geIfPresent(SysErrorLog::getLastOccurrenceTime, req.getStartTime())
                        .leIfPresent(SysErrorLog::getLastOccurrenceTime, req.getEndTime())
                        .orderByDesc(SysErrorLog::getLastOccurrenceTime);
        wrapper.select(
                SysErrorLog::getId,
                SysErrorLog::getUserId,
                SysErrorLog::getUsername,
                SysErrorLog::getModuleName,
                SysErrorLog::getPlatformType,
                SysErrorLog::getSourceType,
                SysErrorLog::getBusinessKey,
                SysErrorLog::getRequestId,
                SysErrorLog::getRequestUrl,
                SysErrorLog::getExceptionClass,
                SysErrorLog::getMessage,
                SysErrorLog::getIp,
                SysErrorLog::getStatus,
                SysErrorLog::getOccurrenceCount,
                SysErrorLog::getFirstOccurrenceTime,
                SysErrorLog::getLastOccurrenceTime,
                SysErrorLog::getHandleRemark);
        var result =
                PageConverter.toResp(
                        page(req.getPageNum(), req.getPageSize(), wrapper)
                                .convert(converter::toResp));
        enrichUsers(result.getRecords());
        return result;
    }

    @Override
    @Transactional
    public void record(SysErrorLog errorLog, SysErrorLogOccurrence occurrence) {
        if (errorLog.getId() == null) {
            errorLog.setId(IdWorker.getId());
        }
        if (errorLog.getCreateTime() == null) {
            errorLog.setCreateTime(errorLog.getFirstOccurrenceTime());
        }
        baseMapper.upsertAggregate(errorLog);
        Long errorLogId = baseMapper.selectIdByFingerprint(errorLog.getFingerprint());
        if (errorLogId == null) {
            throw new IllegalStateException("错误日志聚合记录写入后不存在");
        }
        occurrence.setId(IdWorker.getId());
        occurrence.setErrorLogId(errorLogId);
        occurrenceMapper.insert(occurrence);
    }

    @Override
    public SysErrorLogResp getDetailOrThrow(Long id) {
        var detail = converter.toResp(getRequired(id));
        var occurrences =
                occurrenceMapper.selectList(
                        new LambdaQueryWrapperX<SysErrorLogOccurrence>()
                                .eq(SysErrorLogOccurrence::getErrorLogId, id)
                                .orderByDesc(SysErrorLogOccurrence::getOccurredTime)
                                .last("LIMIT 20"));
        var occurrenceResponses = occurrences.stream().map(converter::toOccurrenceResp).toList();
        detail.setOccurrences(occurrenceResponses);
        enrichUsers(detail, occurrenceResponses);
        return detail;
    }

    @Override
    @Transactional
    public void handle(Long id, SysErrorLogHandleReq req) {
        var errorLog = getRequired(id);
        if (!SysErrorLogHandleStatus.PENDING.getValue().equals(errorLog.getStatus())) {
            throw new BizException(OpsErrorCode.ERROR_LOG_ALREADY_HANDLED);
        }
        var remark = req.getRemark();
        int updated =
                baseMapper.handleIfPending(
                        id,
                        req.getStatus(),
                        StpKit.of(LoginType.ADMIN).getLoginIdAsLong(),
                        LocalDateTime.now(),
                        remark == null || remark.isBlank() ? null : remark.trim());
        if (updated == 0) {
            throw new BizException(OpsErrorCode.ERROR_LOG_ALREADY_HANDLED);
        }
    }

    @Override
    @Transactional
    public int handleAllPending(SysErrorLogHandleReq req) {
        var remark = req.getRemark();
        return baseMapper.handleAllPending(
                req.getStatus(),
                StpKit.of(LoginType.ADMIN).getLoginIdAsLong(),
                LocalDateTime.now(),
                remark == null || remark.isBlank() ? null : remark.trim());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getRequired(id);
        occurrenceMapper.delete(
                new LambdaQueryWrapperX<SysErrorLogOccurrence>()
                        .eq(SysErrorLogOccurrence::getErrorLogId, id));
        removeById(id);
    }

    private SysErrorLog getRequired(Long id) {
        var errorLog = getById(id);
        if (errorLog == null) {
            throw new BizException(OpsErrorCode.ERROR_LOG_NOT_FOUND);
        }
        return errorLog;
    }

    private void enrichUsers(Collection<SysErrorLogResp> records) {
        var userIds = new HashSet<Long>();
        records.forEach(
                record -> {
                    if (record.getHandledBy() != null) {
                        userIds.add(record.getHandledBy());
                    }
                    if (record.getUserId() != null
                            && isBlank(record.getUsername())
                            && !"APP".equals(record.getPlatformType())) {
                        userIds.add(record.getUserId());
                    }
                });
        Map<Long, String> usernames = userApi.getUsernameMapByIds(userIds);
        records.forEach(
                record -> {
                    if (isBlank(record.getUsername()) && !"APP".equals(record.getPlatformType())) {
                        record.setUsername(getUsername(usernames, record.getUserId()));
                    }
                    record.setHandledByUsername(getUsername(usernames, record.getHandledBy()));
                });
    }

    private void enrichUsers(
            SysErrorLogResp detail, Collection<SysErrorLogOccurrenceResp> occurrences) {
        var userIds = new HashSet<Long>();
        if (detail.getHandledBy() != null) {
            userIds.add(detail.getHandledBy());
        }
        if (!"APP".equals(detail.getPlatformType())) {
            if (detail.getUserId() != null && isBlank(detail.getUsername())) {
                userIds.add(detail.getUserId());
            }
            occurrences.stream()
                    .filter(occurrence -> occurrence.getUserId() != null)
                    .filter(occurrence -> isBlank(occurrence.getUsername()))
                    .map(SysErrorLogOccurrenceResp::getUserId)
                    .forEach(userIds::add);
        }
        Map<Long, String> usernames = userApi.getUsernameMapByIds(userIds);
        if (isBlank(detail.getUsername()) && !"APP".equals(detail.getPlatformType())) {
            detail.setUsername(getUsername(usernames, detail.getUserId()));
        }
        detail.setHandledByUsername(getUsername(usernames, detail.getHandledBy()));
        occurrences.forEach(
                occurrence -> {
                    if (isBlank(occurrence.getUsername())
                            && !"APP".equals(detail.getPlatformType())) {
                        occurrence.setUsername(getUsername(usernames, occurrence.getUserId()));
                    }
                });
    }

    private static String getUsername(Map<Long, String> usernames, Long userId) {
        return userId == null ? null : usernames.get(userId);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
