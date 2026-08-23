package com.travis.monolith.ops.job.internal.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.transaction.AfterCommitExecutor;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.infrastructure.framework.quartz.core.QuartzJobHandlerRegistry;
import com.travis.monolith.ops.common.api.enums.OpsErrorCode;
import com.travis.monolith.ops.job.api.enums.OpsJobMisfirePolicy;
import com.travis.monolith.ops.job.api.enums.OpsJobStatus;
import com.travis.monolith.ops.job.api.request.OpsJobCreateReq;
import com.travis.monolith.ops.job.api.request.OpsJobPageReq;
import com.travis.monolith.ops.job.api.request.OpsJobPreviewReq;
import com.travis.monolith.ops.job.api.request.OpsJobUpdateReq;
import com.travis.monolith.ops.job.api.response.OpsJobHandlerResp;
import com.travis.monolith.ops.job.api.response.OpsJobResp;
import com.travis.monolith.ops.job.internal.converter.OpsJobConverter;
import com.travis.monolith.ops.job.internal.entity.OpsJob;
import com.travis.monolith.ops.job.internal.entity.OpsJobLog;
import com.travis.monolith.ops.job.internal.mapper.OpsJobMapper;
import com.travis.monolith.ops.job.internal.model.OpsJobExecutionConfig;
import com.travis.monolith.ops.job.internal.quartz.QuartzJobManager;
import com.travis.monolith.ops.job.internal.service.OpsJobLogService;
import com.travis.monolith.ops.job.internal.service.OpsJobService;
import com.travis.monolith.ops.job.internal.validator.OpsJobParamValidator;
import com.travis.monolith.ops.job.internal.validator.OpsJobScheduleValidator;
import com.travis.monolith.system.user.api.SysUserApi;
import com.travis.monolith.system.user.api.response.SysUserOptionResp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 定时任务管理服务实现，负责任务配置维护、运行控制，并同步 Quartz 调度状态。 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OpsJobServiceImpl extends ServiceImplX<OpsJobMapper, OpsJob> implements OpsJobService {

    private static final int NOT_BUILTIN = 0;
    private static final int BUILTIN = 1;
    private static final String DEFAULT_PARAMS = "{}";
    private static final String COPY_NAME_SUFFIX = "-副本";
    private static final int MAX_JOB_NAME_LENGTH = 120;

    private final QuartzJobManager quartzJobManager;
    private final QuartzJobHandlerRegistry handlerRegistry;
    private final SysUserApi userApi;
    private final OpsJobConverter converter;
    private final OpsJobLogService jobLogService;

    /** 分页查询定时任务。 */
    @Override
    public PageResp<OpsJobResp> page(OpsJobPageReq req) {
        var wrapper =
                new LambdaQueryWrapperX<OpsJob>()
                        .likeIfPresent(OpsJob::getJobName, req.getJobName())
                        .eqIfPresent(OpsJob::getHandlerName, req.getHandlerName())
                        .eqIfPresent(OpsJob::getScheduleType, req.getScheduleType())
                        .eqIfPresent(OpsJob::getStatus, req.getStatus())
                        .orderByDesc(OpsJob::getCreateTime);
        Page<OpsJob> page = page(req.getPageNum(), req.getPageSize(), wrapper);
        Map<Long, OpsJobLog> latestLogs = latestLogMap(page.getRecords());
        Map<Long, String> creatorNames =
                userApi.getUsernameMapByIds(
                        page.getRecords().stream()
                                .map(OpsJob::getCreateBy)
                                .filter(Objects::nonNull)
                                .toList());
        return PageConverter.toResp(
                page.convert(
                        job -> {
                            OpsJobResp response = converter.toResp(job);
                            response.setHandlerAvailable(
                                    handlerRegistry.contains(job.getHandlerName()));
                            response.setCreateByUsername(creatorNames.get(job.getCreateBy()));
                            response.setNextFireTime(quartzJobManager.nextFireTime(job.getId()));
                            OpsJobLog latestLog = latestLogs.get(job.getId());
                            if (latestLog != null) {
                                response.setLastExecutionTime(latestLog.getStartTime());
                                response.setLastExecutionStatus(latestLog.getStatus());
                            }
                            return response;
                        }));
    }

    /** 查询指定定时任务，不存在时抛出业务异常。 */
    @Override
    public OpsJobResp getOrThrow(Long id) {
        OpsJob job = getRequired(id);
        return enrichResponse(converter.toResp(job), job);
    }

    /** 查询执行观察器所需配置，任务不存在时返回空结果。 */
    @Override
    public OpsJobExecutionConfig findExecutionConfig(Long id) {
        OpsJob job = super.getById(id);
        return job == null ? null : converter.toExecutionConfig(job);
    }

    /** 创建定时任务。 */
    @Override
    @Transactional
    @CacheEvict(cacheNames = "ops:job-dashboard", key = "'summary'")
    public void create(OpsJobCreateReq req) {
        validate(req.getHandlerName(), req.getParams());
        validateUserScope(req.getAlertUserIds());
        OpsJob job = converter.toEntity(req);
        normalizeDefaults(job);
        OpsJobScheduleValidator.validate(job);
        job.setIsBuiltin(NOT_BUILTIN);
        job.setStatus(OpsJobStatus.DISABLED.getValue());
        save(job);
        synchronizeAfterCommit(job);
    }

    /** 更新指定定时任务。 */
    @Override
    @Transactional
    public void update(Long id, OpsJobUpdateReq req) {
        OpsJob job = getRequired(id);
        ensureNotBuiltin(job);
        validate(req.getHandlerName(), req.getParams());
        validateUserScope(req.getAlertUserIds());
        converter.update(req, job);
        normalizeDefaults(job);
        OpsJobScheduleValidator.validate(job);
        job.setLockVersion(req.getLockVersion());
        if (OpsJobStatus.ENABLED.getValue().equals(job.getStatus())) {
            ensureHandlerExists(job.getHandlerName());
        }
        updateOrThrow(job);
        synchronizeAfterCommit(job);
    }

    /** 删除指定定时任务。 */
    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(cacheNames = "ops:job-stats", key = "#id"),
                @CacheEvict(cacheNames = "ops:job-dashboard", key = "'summary'")
            })
    public void delete(Long id) {
        ensureNotBuiltin(getRequired(id));
        removeById(id);
        runAfterCommit(id, () -> quartzJobManager.delete(id));
    }

    /** 变更指定定时任务的状态。 */
    @Override
    @Transactional
    @CacheEvict(cacheNames = "ops:job-dashboard", key = "'summary'")
    public void changeStatus(Long id, Integer status) {
        OpsJob job = getRequired(id);
        ensureNotBuiltin(job);
        if (OpsJobStatus.ENABLED.getValue().equals(status)) {
            ensureHandlerExists(job.getHandlerName());
            OpsJobScheduleValidator.validate(job);
            job.setStatus(OpsJobStatus.ENABLED.getValue());
        } else {
            job.setStatus(OpsJobStatus.DISABLED.getValue());
        }
        updateOrThrow(job);
        synchronizeAfterCommit(job);
    }

    /** 立即运行指定定时任务。 */
    @Override
    public void runNow(Long id, String params) {
        OpsJob job = getRequired(id);
        ensureHandlerExists(job.getHandlerName());
        OpsJobParamValidator.validate(params == null ? job.getParams() : params);
        quartzJobManager.runNow(job, params);
    }

    /** 复制指定定时任务。 */
    @Override
    @Transactional
    @CacheEvict(cacheNames = "ops:job-dashboard", key = "'summary'")
    public void copy(Long id) {
        OpsJob source = getRequired(id);
        ensureNotBuiltin(source);
        var copy = converter.copy(source);
        copy.setJobName(copyName(source.getJobName()));
        copy.setStatus(OpsJobStatus.DISABLED.getValue());
        copy.setIsBuiltin(NOT_BUILTIN);
        normalizeDefaults(copy);
        OpsJobScheduleValidator.validate(copy);
        save(copy);
        synchronizeAfterCommit(copy);
    }

    /** 当前配置对应的单次计划执行结束后将任务恢复为停用状态。 */
    @Override
    @Transactional
    @CacheEvict(cacheNames = "ops:job-dashboard", key = "'summary'")
    public void completeOnce(Long id, String configFingerprint) {
        OpsJob job = super.getById(id);
        if (job == null
                || !"ONCE".equals(job.getScheduleType())
                || !OpsJobStatus.ENABLED.getValue().equals(job.getStatus())
                || !Objects.equals(configFingerprint, quartzJobManager.configFingerprint(job))) {
            return;
        }
        job.setStatus(OpsJobStatus.DISABLED.getValue());
        updateById(job);
    }

    /** 预览定时任务后续执行时间。 */
    @Override
    public List<LocalDateTime> preview(OpsJobPreviewReq req, Integer count) {
        OpsJob job = converter.toEntity(req);
        OpsJobScheduleValidator.validate(job);
        return quartzJobManager.preview(job, count == null ? 5 : count);
    }

    /** 查询已注册的定时任务处理器名称及说明。 */
    @Override
    public List<OpsJobHandlerResp> listHandlers(boolean includeBuiltin) {
        return handlerRegistry.descriptors(includeBuiltin).stream()
                .map(handler -> new OpsJobHandlerResp(handler.name(), handler.description()))
                .toList();
    }

    /** 查询定时任务告警接收人选项。 */
    @Override
    public List<SysUserOptionResp> listUserOptions(String keyword, Collection<Long> userIds) {
        if (userIds != null && !userIds.isEmpty()) {
            return userApi.listCurrentUserScopedOptionsByIds(userIds);
        }
        return userApi.listCurrentUserScopedOptions(keyword, 20);
    }

    /** 校验定时任务写入参数。 */
    private void validate(String handlerName, String params) {
        ensureNotBuiltinHandler(handlerName);
        OpsJobParamValidator.validate(params);
    }

    /** 校验定时任务通知用户范围。 */
    private void validateUserScope(Collection<Long> alertUserIds) {
        Set<Long> userIds =
                alertUserIds == null
                        ? Set.of()
                        : alertUserIds.stream()
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
            throw new BizException(OpsErrorCode.USER_OUT_OF_SCOPE);
        }
    }

    /** 校验定时任务处理器是否已注册。 */
    private void ensureHandlerExists(String handlerName) {
        if (!handlerRegistry.contains(handlerName)) {
            throw new BizException(OpsErrorCode.HANDLER_NOT_FOUND, handlerName);
        }
    }

    /** 校验处理器没有被系统内置任务保留。 */
    private void ensureNotBuiltinHandler(String handlerName) {
        if (handlerRegistry.isBuiltin(handlerName)) {
            throw new BizException(OpsErrorCode.BUILTIN_HANDLER_RESERVED, handlerName);
        }
    }

    /** 校验任务不是由系统维护的内置任务。 */
    private void ensureNotBuiltin(OpsJob job) {
        if (Objects.equals(job.getIsBuiltin(), BUILTIN)) {
            throw new BizException(OpsErrorCode.BUILTIN_NOT_MODIFIABLE);
        }
    }

    /** 批量查询当前分页任务的最近一次执行日志。 */
    private Map<Long, OpsJobLog> latestLogMap(List<OpsJob> jobs) {
        List<Long> jobIds = jobs.stream().map(OpsJob::getId).filter(Objects::nonNull).toList();
        if (jobIds.isEmpty()) {
            return Map.of();
        }
        return jobLogService.latestByJobIds(jobIds);
    }

    /** 统一补充任务持久化和调度所需的默认配置。 */
    private void normalizeDefaults(OpsJob job) {
        if (job.getMisfirePolicy() == null) {
            job.setMisfirePolicy(OpsJobMisfirePolicy.SMART.getValue());
        }
        if (job.getParams() == null || job.getParams().isBlank()) {
            job.setParams(DEFAULT_PARAMS);
        }
    }

    /** 生成不超过数据库字段长度的复制任务名称。 */
    private String copyName(String sourceName) {
        int maxSourceLength = MAX_JOB_NAME_LENGTH - COPY_NAME_SUFFIX.length();
        String normalizedSource =
                sourceName.length() <= maxSourceLength
                        ? sourceName
                        : sourceName.substring(0, maxSourceLength);
        return normalizedSource + COPY_NAME_SUFFIX;
    }

    /** 事务提交后将当前业务配置同步至 Quartz。 */
    private void synchronizeAfterCommit(OpsJob job) {
        runAfterCommit(job.getId(), () -> quartzJobManager.schedule(job));
    }

    /** 在事务确认提交后执行 Quartz 操作，失败时由周期对账恢复。 */
    private void runAfterCommit(Long jobId, Runnable action) {
        Runnable guardedAction =
                () -> {
                    try {
                        action.run();
                    } catch (Exception exception) {
                        log.error("同步 Quartz 任务失败，jobId={}", jobId, exception);
                    }
                };
        AfterCommitExecutor.execute(guardedAction);
    }

    /** 查询指定定时任务实体，确保记录存在。 */
    private OpsJob getRequired(Long id) {
        OpsJob job = getById(id);
        if (job == null) {
            throw new BizException(OpsErrorCode.JOB_NOT_FOUND);
        }
        return job;
    }

    /** 更新调度任务，检测并发覆盖。 */
    private void updateOrThrow(OpsJob job) {
        if (!updateById(job)) {
            throw new BizException(OpsErrorCode.CONCURRENT_UPDATE);
        }
    }

    /** 补充定时任务响应中的展示信息。 */
    private OpsJobResp enrichResponse(OpsJobResp response, OpsJob job) {
        response.setHandlerAvailable(handlerRegistry.contains(job.getHandlerName()));
        response.setNextFireTime(quartzJobManager.nextFireTime(job.getId()));
        return response;
    }
}
