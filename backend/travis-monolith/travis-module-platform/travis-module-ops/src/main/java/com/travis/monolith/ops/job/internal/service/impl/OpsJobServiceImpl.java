package com.travis.monolith.ops.job.internal.service.impl;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.infrastructure.framework.quartz.core.QuartzJobHandlerRegistry;
import com.travis.monolith.ops.job.api.OpsJobErrorCode;
import com.travis.monolith.ops.job.api.enums.OpsJobStatus;
import com.travis.monolith.ops.job.api.request.*;
import com.travis.monolith.ops.job.api.response.OpsJobBaseResp;
import com.travis.monolith.ops.job.api.response.OpsJobDetailResp;
import com.travis.monolith.ops.job.api.response.OpsJobExportResp;
import com.travis.monolith.ops.job.api.response.OpsJobPageResp;
import com.travis.monolith.ops.job.internal.converter.OpsJobConverter;
import com.travis.monolith.ops.job.internal.entity.OpsJob;
import com.travis.monolith.ops.job.internal.mapper.OpsJobMapper;
import com.travis.monolith.ops.job.internal.service.OpsJobParamValidator;
import com.travis.monolith.ops.job.internal.service.OpsJobService;
import com.travis.monolith.ops.job.internal.service.QuartzJobManager;
import com.travis.monolith.system.user.api.SysUserApi;
import com.travis.monolith.system.user.api.response.SysUserOptionResp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "ops:job")
public class OpsJobServiceImpl extends ServiceImplX<OpsJobMapper, OpsJob> implements OpsJobService {

    private static final Map<String, SFunction<OpsJob, ?>> SORT_COLUMNS =
            Map.of(
                    "jobName", OpsJob::getJobName,
                    "handlerName", OpsJob::getHandlerName,
                    "status", OpsJob::getStatus,
                    "createTime", OpsJob::getCreateTime,
                    "updateTime", OpsJob::getUpdateTime);

    private final QuartzJobManager quartzJobManager;
    private final QuartzJobHandlerRegistry handlerRegistry;
    private final SysUserApi userApi;
    private final OpsJobConverter converter;

    @Override
    public PageResp<OpsJobPageResp> page(OpsJobPageReq req) {
        var wrapper =
                new LambdaQueryWrapperX<OpsJob>()
                        .likeIfPresent(OpsJob::getJobName, req.getJobName())
                        .likeIfPresent(OpsJob::getHandlerName, req.getHandlerName())
                        .eqIfPresent(OpsJob::getScheduleType, req.getScheduleType())
                        .eqIfPresent(OpsJob::getStatus, req.getStatus())
                        .eqIfPresent(OpsJob::getOwnerUserId, req.getOwnerUserId())
                        .orderByAllowed(
                                req.getOrderBy(),
                                req.getAsc(),
                                SORT_COLUMNS,
                                false,
                                OpsJob::getCreateTime);
        Page<OpsJob> page = page(req.getPageNum(), req.getPageSize(), wrapper);
        Map<Long, String> ownerNames =
                userApi.getUsernameMapByIds(
                        page.getRecords().stream()
                                .map(OpsJob::getOwnerUserId)
                                .filter(java.util.Objects::nonNull)
                                .toList());
        return PageConverter.toResp(
                page.convert(
                        job ->
                                enrichResponse(
                                        converter.toPageResp(job),
                                        job,
                                        ownerNames.get(job.getOwnerUserId()))));
    }

    @Override
    @Cacheable(key = "'detail:'+#id")
    public OpsJobDetailResp getOrThrow(Long id) {
        OpsJob job = getRequired(id);
        return enrichResponse(
                converter.toDetailResp(job), job, userApi.getUsernameById(job.getOwnerUserId()));
    }

    @Override
    @Cacheable(key = "'detail:'+#id", unless = "#result == null")
    public OpsJobDetailResp find(Long id) {
        OpsJob job = super.getById(id);
        return job == null
                ? null
                : enrichResponse(
                        converter.toDetailResp(job),
                        job,
                        userApi.getUsernameById(job.getOwnerUserId()));
    }

    @Override
    public List<OpsJob> listAll() {
        return list();
    }

    @Override
    public long countJobs(Integer status) {
        return count(new LambdaQueryWrapperX<OpsJob>().eqIfPresent(OpsJob::getStatus, status));
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void create(OpsJobCreateReq req) {
        createJob(req);
    }

    private void createJob(OpsJobWriteReq req) {
        validateUserScope(req);
        OpsJob job = buildEntity(req);
        job.setStatus(OpsJobStatus.DISABLED.getValue());
        save(job);
        quartzJobManager.schedule(job);
    }

    @Override
    @Transactional
    @CacheEvict(key = "'detail:'+#id")
    public void update(Long id, OpsJobUpdateReq req) {
        OpsJob job = getRequired(id);
        validateUserScope(req);
        converter.update(req, job);
        updateById(job);
        quartzJobManager.schedule(job);
    }

    @Override
    @Transactional
    @CacheEvict(key = "'detail:'+#id")
    public void delete(Long id) {
        getRequired(id);
        quartzJobManager.delete(id);
        removeById(id);
    }

    @Override
    @Transactional
    @CacheEvict(key = "'detail:'+#id")
    public void changeStatus(Long id, Integer status) {
        OpsJob job = getRequired(id);
        if (OpsJobStatus.ENABLED.getValue().equals(status)) {
            ensureHandlerExists(job.getHandlerName());
            quartzJobManager.resume(id);
            job.setStatus(OpsJobStatus.ENABLED.getValue());
        } else {
            quartzJobManager.pause(id);
            job.setStatus(OpsJobStatus.DISABLED.getValue());
        }
        updateById(job);
    }

    @Override
    public void runNow(Long id, String params) {
        OpsJob job = getRequired(id);
        ensureHandlerExists(job.getHandlerName());
        OpsJobParamValidator.validate(
                params == null ? job.getParams() : params, job.getParamSchema());
        quartzJobManager.runNow(job, params);
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void copy(Long id) {
        OpsJob source = getRequired(id);
        var copy = converter.copy(source);
        copy.setJobName(source.getJobName() + "-副本");
        copy.setStatus(OpsJobStatus.DISABLED.getValue());
        save(copy);
        quartzJobManager.schedule(copy);
    }

    @Override
    public List<LocalDateTime> preview(OpsJobPreviewReq req, Integer count) {
        return quartzJobManager.preview(buildEntity(req), count == null ? 5 : count);
    }

    @Override
    public Collection<String> listHandlers() {
        return handlerRegistry.names();
    }

    @Override
    public List<SysUserOptionResp> listUserOptions(String keyword, Collection<Long> userIds) {
        if (userIds != null && !userIds.isEmpty()) {
            return userApi.listCurrentUserScopedOptionsByIds(userIds);
        }
        return userApi.listCurrentUserScopedOptions(keyword, 20);
    }

    @Override
    public List<OpsJobExportResp> exportJobs() {
        return list(new LambdaQueryWrapperX<OpsJob>().orderByAsc(OpsJob::getJobName)).stream()
                .map(
                        job ->
                                enrichResponse(
                                        converter.toExportResp(job),
                                        job,
                                        userApi.getUsernameById(job.getOwnerUserId())))
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void importJobs(List<OpsJobImportReq> jobs) {
        if (jobs == null) {
            return;
        }
        jobs.forEach(this::createJob);
    }

    private OpsJob buildEntity(OpsJobWriteReq req) {
        validate(req);
        return converter.toEntity(req);
    }

    private void validate(OpsJobWriteReq req) {
        OpsJobParamValidator.validate(req.getParams(), req.getParamSchema());
        if ((req.getDailyStartTime() == null) != (req.getDailyEndTime() == null)) {
            throw new BizException(OpsJobErrorCode.INVALID_SCHEDULE, "每日开始和结束时间必须同时填写");
        }
        if (req.getDailyStartTime() != null
                && !req.getDailyStartTime().isBefore(req.getDailyEndTime())) {
            throw new BizException(OpsJobErrorCode.INVALID_SCHEDULE, "每日开始时间必须早于结束时间");
        }
    }

    private void validateUserScope(OpsJobWriteReq req) {
        Set<Long> userIds =
                java.util.stream.Stream.concat(
                                java.util.stream.Stream.of(req.getOwnerUserId()),
                                req.getAlertUserIds() == null
                                        ? java.util.stream.Stream.empty()
                                        : req.getAlertUserIds().stream())
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return;
        }
        Set<Long> allowedUserIds =
                userApi.listCurrentUserScopedOptionsByIds(userIds).stream()
                        .map(SysUserOptionResp::getId)
                        .collect(Collectors.toSet());
        if (!allowedUserIds.containsAll(userIds)) {
            throw new BizException(OpsJobErrorCode.USER_OUT_OF_SCOPE);
        }
    }

    private void ensureHandlerExists(String handlerName) {
        if (!handlerRegistry.contains(handlerName)) {
            throw new BizException(OpsJobErrorCode.HANDLER_NOT_FOUND, handlerName);
        }
    }

    private OpsJob getRequired(Long id) {
        OpsJob job = getById(id);
        if (job == null) {
            throw new BizException(OpsJobErrorCode.JOB_NOT_FOUND);
        }
        return job;
    }

    private <T extends OpsJobBaseResp> T enrichResponse(
            T response, OpsJob job, String ownerUsername) {
        response.setOwnerUsername(ownerUsername);
        response.setHandlerAvailable(handlerRegistry.contains(job.getHandlerName()));
        response.setNextFireTime(quartzJobManager.nextFireTime(job.getId()));
        return response;
    }
}
