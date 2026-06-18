package com.travis.monolith.system.config.internal.service.impl;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.monolith.system.common.api.enums.SystemErrorCode;
import com.travis.monolith.system.config.api.request.SysConfigCreateReq;
import com.travis.monolith.system.config.api.request.SysConfigPageReq;
import com.travis.monolith.system.config.api.request.SysConfigUpdateReq;
import com.travis.monolith.system.config.api.response.SysConfigDetailResp;
import com.travis.monolith.system.config.api.response.SysConfigPageResp;
import com.travis.monolith.system.config.internal.converter.SysConfigConverter;
import com.travis.monolith.system.config.internal.entity.SysConfig;
import com.travis.monolith.system.config.internal.mapper.SysConfigMapper;
import com.travis.monolith.system.config.internal.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 系统配置服务实现
 *
 * @author travis
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "system:config")
public class SysConfigServiceImpl extends ServiceImplX<SysConfigMapper, SysConfig>
        implements SysConfigService {

    private static final Map<String, SFunction<SysConfig, ?>> SORT_COLUMNS =
            Map.of(
                    "configKey", SysConfig::getConfigKey,
                    "createTime", SysConfig::getCreateTime,
                    "updateTime", SysConfig::getUpdateTime);

    private final SysConfigConverter converter;

    @Override
    public PageResp<SysConfigPageResp> page(SysConfigPageReq req) {
        var wrapper =
                new LambdaQueryWrapperX<SysConfig>()
                        .likeIfPresent(SysConfig::getConfigKey, req.getConfigKey())
                        .orderByAllowed(
                                req.getOrderBy(),
                                req.getAsc(),
                                SORT_COLUMNS,
                                true,
                                SysConfig::getConfigKey);
        Page<SysConfig> page = page(req.getPageNum(), req.getPageSize(), wrapper);
        return PageConverter.toResp(page.convert(converter::toResp));
    }

    @Override
    @Cacheable(key = "'detail:'+#id")
    public SysConfigDetailResp getById(Long id) {
        return converter.toDetailResp(getByIdOrThrow(id));
    }

    @Override
    @Cacheable(key = "'value:'+#configKey")
    public String getValueByKey(String configKey) {
        var config =
                getOne(new LambdaQueryWrapperX<SysConfig>().eq(SysConfig::getConfigKey, configKey));
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void create(SysConfigCreateReq req) {
        SysConfig entity = converter.toEntity(req);
        save(entity);
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void update(Long id, SysConfigUpdateReq req) {
        SysConfig entity = getConfigOrThrow(id);
        checkBuiltinKey(entity, req);
        converter.update(req, entity);
        updateById(entity);
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void deleteById(Long id) {
        SysConfig entity = getConfigOrThrow(id);
        checkDeletable(entity);
        removeById(id);
    }

    private SysConfig getConfigOrThrow(Long id) {
        return getByIdOrThrow(id);
    }

    private void checkBuiltinKey(SysConfig entity, SysConfigUpdateReq req) {
        if (Integer.valueOf(1).equals(entity.getIsBuiltin())
                && !entity.getConfigKey().equals(req.getConfigKey())) {
            throw new BizException(SystemErrorCode.CONFIG_BUILTIN_KEY_NOT_MODIFIABLE);
        }
    }

    private void checkDeletable(SysConfig entity) {
        if (Integer.valueOf(1).equals(entity.getIsBuiltin())) {
            throw new BizException(SystemErrorCode.CONFIG_BUILTIN_NOT_DELETABLE);
        }
    }
}
