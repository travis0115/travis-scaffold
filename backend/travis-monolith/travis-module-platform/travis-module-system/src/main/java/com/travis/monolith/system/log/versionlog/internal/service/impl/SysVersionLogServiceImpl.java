package com.travis.monolith.system.log.versionlog.internal.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.monolith.system.log.versionlog.api.request.SysVersionLogCreateReq;
import com.travis.monolith.system.log.versionlog.api.request.SysVersionLogUpdateReq;
import com.travis.monolith.system.log.versionlog.api.response.SysVersionLogDetailResp;
import com.travis.monolith.system.log.versionlog.api.response.SysVersionLogPageResp;
import com.travis.monolith.system.log.versionlog.api.response.SysVersionLogPublishedResp;
import com.travis.monolith.system.log.versionlog.internal.converter.SysVersionLogConverter;
import com.travis.monolith.system.log.versionlog.internal.entity.SysVersionLog;
import com.travis.monolith.system.log.versionlog.internal.mapper.SysVersionLogMapper;
import com.travis.monolith.system.log.versionlog.internal.service.SysVersionLogService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统版本日志服务实现
 *
 * @author travis
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "system:version-log")
public class SysVersionLogServiceImpl extends ServiceImplX<SysVersionLogMapper, SysVersionLog>
        implements SysVersionLogService {

    private final SysVersionLogConverter converter;

    @Override
    public PageResp<SysVersionLogPageResp> page(
            String version, String title, Integer status, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapperX<SysVersionLog> wrapper =
                new LambdaQueryWrapperX<SysVersionLog>()
                        .likeIfPresent(SysVersionLog::getVersion, version)
                        .likeIfPresent(SysVersionLog::getTitle, title)
                        .eqIfPresent(SysVersionLog::getStatus, status)
                        .orderByDesc(SysVersionLog::getCreateTime);
        Page<SysVersionLog> page = page(pageNum, pageSize, wrapper);
        return PageConverter.toResp(page.convert(converter::toResp));
    }

    @Override
    @Cacheable(key = "'detail:'+#id")
    public SysVersionLogDetailResp getById(Long id) {
        return converter.toDetailResp(getByIdOrThrow(id));
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void create(SysVersionLogCreateReq req) {
        SysVersionLog entity = converter.toEntity(req);
        save(entity);
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void update(Long id, SysVersionLogUpdateReq req) {
        SysVersionLog entity = getByIdOrThrow(id);
        converter.update(req, entity);
        updateById(entity);
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void updateStatus(Long id, Integer status) {
        SysVersionLog entity = getByIdOrThrow(id);
        entity.setStatus(status);
        updateById(entity);
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void deleteById(Long id) {
        removeById(id);
    }

    @Override
    @Cacheable(key = "'published-list:' + (#limit == null || #limit <= 0 ? 10 : #limit)")
    public List<SysVersionLogPublishedResp> listPublished(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        LambdaQueryWrapperX<SysVersionLog> wrapper =
                new LambdaQueryWrapperX<SysVersionLog>()
                        .eq(SysVersionLog::getStatus, 1)
                        .orderByDesc(SysVersionLog::getPublishTime);
        Page<SysVersionLog> page = page(1, limit, wrapper);
        return converter.toPublishedRespList(page.getRecords());
    }
}
