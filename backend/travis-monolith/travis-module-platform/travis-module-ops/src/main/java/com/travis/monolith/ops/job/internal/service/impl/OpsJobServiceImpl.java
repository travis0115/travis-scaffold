package com.travis.monolith.ops.job.internal.service.impl;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.jackson.core.JsonUtil;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.quartz.core.QuartzJobHandlerRegistry;
import com.travis.monolith.ops.job.api.OpsJobErrorCode;
import com.travis.monolith.ops.job.api.request.OpsJobCreateReq;
import com.travis.monolith.ops.job.api.request.OpsJobImportReq;
import com.travis.monolith.ops.job.api.request.OpsJobPageReq;
import com.travis.monolith.ops.job.api.request.OpsJobPreviewReq;
import com.travis.monolith.ops.job.api.request.OpsJobUpdateReq;
import com.travis.monolith.ops.job.api.request.OpsJobWriteReq;
import com.travis.monolith.ops.job.api.response.OpsJobBaseResp;
import com.travis.monolith.ops.job.api.response.OpsJobDetailResp;
import com.travis.monolith.ops.job.api.response.OpsJobExportResp;
import com.travis.monolith.ops.job.api.response.OpsJobPageResp;
import com.travis.monolith.ops.job.internal.entity.OpsJob;
import com.travis.monolith.ops.job.internal.mapper.OpsJobMapper;
import com.travis.monolith.ops.job.internal.model.OpsJobCalendarConfig;
import com.travis.monolith.ops.job.internal.service.OpsJobParamValidator;
import com.travis.monolith.ops.job.internal.service.OpsJobService;
import com.travis.monolith.ops.job.internal.service.QuartzJobManager;
import com.travis.monolith.system.user.api.SysUserApi;
import com.travis.monolith.system.user.api.response.SysUserOptionResp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OpsJobServiceImpl extends ServiceImpl<OpsJobMapper, OpsJob> implements OpsJobService {

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
        Page<OpsJob> page = page(new Page<>(req.getPageNum(), req.getPageSize()), wrapper);
        Map<Long, String> ownerNames =
                userApi.getUsernameMapByIds(
                        page.getRecords().stream()
                                .map(OpsJob::getOwnerUserId)
                                .filter(java.util.Objects::nonNull)
                                .toList());
        return PageConverter.toResp(
                page.convert(
                        job ->
                                toResponse(
                                        job,
                                        ownerNames.get(job.getOwnerUserId()),
                                        new OpsJobPageResp())));
    }

    @Override
    public OpsJobDetailResp getDetail(Long id) {
        OpsJob job = getRequired(id);
        return toResponse(
                job, userApi.getUsernameById(job.getOwnerUserId()), new OpsJobDetailResp());
    }

    @Override
    @Transactional
    public void create(OpsJobCreateReq req) {
        createJob(req);
    }

    private void createJob(OpsJobWriteReq req) {
        validateUserScope(req);
        OpsJob job = buildEntity(req);
        job.setStatus(0);
        save(job);
        quartzJobManager.schedule(job);
    }

    @Override
    @Transactional
    public void update(Long id, OpsJobUpdateReq req) {
        OpsJob job = getRequired(id);
        validateUserScope(req);
        Integer status = job.getStatus();
        copyRequest(req, job);
        job.setStatus(status);
        updateById(job);
        quartzJobManager.schedule(job);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getRequired(id);
        quartzJobManager.delete(id);
        removeById(id);
    }

    @Override
    @Transactional
    public void changeStatus(Long id, Integer status) {
        OpsJob job = getRequired(id);
        if (Integer.valueOf(1).equals(status)) {
            ensureHandlerExists(job.getHandlerName());
            quartzJobManager.resume(id);
            job.setStatus(1);
        } else {
            quartzJobManager.pause(id);
            job.setStatus(0);
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
    public void copy(Long id) {
        OpsJob source = getRequired(id);
        var copy = new OpsJob();
        BeanUtils.copyProperties(
                source,
                copy,
                "id",
                "createTime",
                "createBy",
                "updateTime",
                "updateBy",
                "isDeleted");
        copy.setJobName(source.getJobName() + "-副本");
        copy.setStatus(0);
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
                                toResponse(
                                        job,
                                        userApi.getUsernameById(job.getOwnerUserId()),
                                        new OpsJobExportResp()))
                .toList();
    }

    @Override
    @Transactional
    public void importJobs(List<OpsJobImportReq> jobs) {
        if (jobs == null) {
            return;
        }
        jobs.forEach(this::createJob);
    }

    private OpsJob buildEntity(OpsJobWriteReq req) {
        validate(req);
        var job = new OpsJob();
        copyRequest(req, job);
        return job;
    }

    private void copyRequest(OpsJobWriteReq req, OpsJob job) {
        BeanUtils.copyProperties(
                req,
                job,
                "excludedDates",
                "excludedWeekdays",
                "dailyStartTime",
                "dailyEndTime",
                "alertUserIds");
        var calendar =
                new OpsJobCalendarConfig(
                        req.getExcludedDates(),
                        req.getExcludedWeekdays(),
                        req.getDailyStartTime(),
                        req.getDailyEndTime());
        job.setCalendarConfig(calendar.isEmpty() ? null : JsonUtil.toJsonString(calendar));
        job.setAlertUserIds(serializeIds(req.getAlertUserIds()));
        job.setPriority(req.getPriority() == null ? 5 : req.getPriority());
        job.setMisfirePolicy(req.getMisfirePolicy() == null ? 0 : req.getMisfirePolicy());
        job.setLogRetentionDays(req.getLogRetentionDays() == null ? 30 : req.getLogRetentionDays());
        job.setParams(
                req.getParams() == null || req.getParams().isBlank() ? "{}" : req.getParams());
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

    private <T extends OpsJobBaseResp> T toResponse(OpsJob job, String ownerUsername, T response) {
        BeanUtils.copyProperties(job, response);
        response.setOwnerUsername(ownerUsername);
        response.setHandlerAvailable(handlerRegistry.contains(job.getHandlerName()));
        response.setAlertUserIds(parseIds(job.getAlertUserIds()));
        if (job.getCalendarConfig() != null) {
            OpsJobCalendarConfig config =
                    JsonUtil.parseObject(job.getCalendarConfig(), OpsJobCalendarConfig.class);
            response.setExcludedDates(config.excludedDates());
            response.setExcludedWeekdays(config.excludedWeekdays());
            response.setDailyStartTime(config.dailyStartTime());
            response.setDailyEndTime(config.dailyEndTime());
        }
        response.setNextFireTime(quartzJobManager.nextFireTime(job.getId()));
        return response;
    }

    private String serializeIds(List<Long> ids) {
        return ids == null || ids.isEmpty()
                ? null
                : ids.stream()
                        .map(String::valueOf)
                        .collect(java.util.stream.Collectors.joining(","));
    }

    private List<Long> parseIds(String ids) {
        return ids == null || ids.isBlank()
                ? List.of()
                : java.util.Arrays.stream(ids.split(",")).map(Long::valueOf).toList();
    }
}
