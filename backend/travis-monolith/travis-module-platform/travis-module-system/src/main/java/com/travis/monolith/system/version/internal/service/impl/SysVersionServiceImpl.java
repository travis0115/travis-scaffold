package com.travis.monolith.system.version.internal.service.impl;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.model.PageRequest;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.monolith.system.common.api.enums.Status;
import com.travis.monolith.system.common.api.enums.SystemErrorCode;
import com.travis.monolith.system.version.api.request.SysVersionCreateReq;
import com.travis.monolith.system.version.api.request.SysVersionPageReq;
import com.travis.monolith.system.version.api.request.SysVersionUpdateReq;
import com.travis.monolith.system.version.api.response.SysVersionResp;
import com.travis.monolith.system.version.internal.converter.SysVersionConverter;
import com.travis.monolith.system.version.internal.entity.SysVersion;
import com.travis.monolith.system.version.internal.mapper.SysVersionMapper;
import com.travis.monolith.system.version.internal.service.SysVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 系统版本日志服务实现
 *
 * @author travis
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "system:version")
public class SysVersionServiceImpl extends ServiceImplX<SysVersionMapper, SysVersion>
        implements SysVersionService {

    private final SysVersionConverter converter;

    private static final Map<String, SFunction<SysVersion, ?>> SORT_COLUMNS =
            Map.of(
                    "publishTime",
                    SysVersion::getPublishTime,
                    "createTime",
                    SysVersion::getCreateTime);

    @Override
    public PageResp<SysVersionResp> page(SysVersionPageReq req) {
        var wrapper =
                new LambdaQueryWrapperX<SysVersion>()
                        .likeIfPresent(SysVersion::getVersion, req.getVersion())
                        .likeIfPresent(SysVersion::getTitle, req.getTitle())
                        .eqIfPresent(SysVersion::getStatus, req.getStatus())
                        .orderByAllowed(
                                req.getOrderBy(),
                                req.getAsc(),
                                SORT_COLUMNS,
                                false,
                                SysVersion::getCreateTime);
        var page = page(req.getPageNum(), req.getPageSize(), wrapper);
        return PageConverter.toResp(page.convert(converter::toResp));
    }

    @Override
    @Cacheable(key = "'detail:'+#id")
    public SysVersionResp getById(Long id) {
        return converter.toResp(getByIdOrThrow(id));
    }

    @Override
    @Transactional
    public void create(SysVersionCreateReq req) {
        var count =
                count(
                        new LambdaQueryWrapperX<SysVersion>()
                                .eq(SysVersion::getVersion, req.getVersion()));
        if (count > 0) {
            throw new BizException(SystemErrorCode.VERSION_EXISTS);
        }
        var entity = converter.toEntity(req);
        save(entity);
    }

    @Override
    @Transactional
    @CacheEvict(key = "'detail:'+#id")
    public void update(Long id, SysVersionUpdateReq req) {
        var entity = getByIdOrThrow(id);
        var count =
                count(
                        new LambdaQueryWrapperX<SysVersion>()
                                .eq(SysVersion::getVersion, req.getVersion())
                                .ne(SysVersion::getId, id));
        if (count > 0) {
            throw new BizException(SystemErrorCode.VERSION_EXISTS);
        }
        converter.update(req, entity);
        updateById(entity);
    }

    @Override
    @Transactional
    @CacheEvict(key = "'detail:'+#id")
    public void updateStatus(Long id, Integer status) {
        var entity = getByIdOrThrow(id);
        entity.setStatus(status);
        updateById(entity);
    }

    @Override
    public PageResp<SysVersionResp> pagePublished(PageRequest req) {
        var wrapper =
                new LambdaQueryWrapperX<SysVersion>()
                        .eq(SysVersion::getStatus, Status.ENABLED.getValue())
                        .orderByDesc(SysVersion::getPublishTime)
                        .orderByDesc(SysVersion::getCreateTime);
        var page = page(req.getPageNum(), req.getPageSize(), wrapper);
        return PageConverter.toResp(page.convert(converter::toResp));
    }

    @Override
    @Transactional
    @CacheEvict(key = "'detail:'+#id")
    public void deleteById(Long id) {
        removeById(id);
    }
}
