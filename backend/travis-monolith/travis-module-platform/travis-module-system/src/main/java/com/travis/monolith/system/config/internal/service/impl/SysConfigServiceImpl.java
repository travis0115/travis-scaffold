package com.travis.monolith.system.config.internal.service.impl;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.infrastructure.framework.redis.core.util.RedisUtil;
import com.travis.monolith.system.common.api.BuiltinResourceGuard;
import com.travis.monolith.system.common.api.enums.SystemErrorCode;
import com.travis.monolith.system.config.api.request.SysConfigCreateReq;
import com.travis.monolith.system.config.api.request.SysConfigPageReq;
import com.travis.monolith.system.config.api.request.SysConfigUpdateReq;
import com.travis.monolith.system.config.api.response.SysConfigResp;
import com.travis.monolith.system.config.internal.converter.SysConfigConverter;
import com.travis.monolith.system.config.internal.entity.SysConfig;
import com.travis.monolith.system.config.internal.mapper.SysConfigMapper;
import com.travis.monolith.system.config.internal.service.SysConfigService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private static final Map<String, SFunction<SysConfig, ?>> SORT_COLUMNS = Map.of();

    private final SysConfigConverter converter;
    private final BuiltinResourceGuard builtinResourceGuard;

    /** 分页查询系统配置。 */
    @Override
    public PageResp<SysConfigResp> page(SysConfigPageReq req) {
        var wrapper =
                new LambdaQueryWrapperX<SysConfig>()
                        .likeIfPresent(SysConfig::getConfigKey, req.getConfigKey())
                        .orderByAllowed(
                                req.getOrderBy(),
                                req.getAsc(),
                                SORT_COLUMNS,
                                true,
                                SysConfig::getCreateTime);
        var page = page(req.getPageNum(), req.getPageSize(), wrapper);
        return PageConverter.toResp(page.convert(converter::toResp));
    }

    /** 查询指定系统配置详情，不存在时抛出业务异常。 */
    @Override
    @Cacheable(key = "'detail:id:'+#id")
    public SysConfigResp getDetailByIdOrThrow(Long id) {
        return converter.toResp(getByIdOrThrow(id));
    }

    /** 根据配置键查询系统配置，不存在时抛出业务异常。 */
    @Override
    @Cacheable(key = "'detail:key:'+#configKey")
    public SysConfigResp getByKeyOrThrow(String configKey) {
        var config =
                getOneOrThrow(
                        new LambdaQueryWrapperX<SysConfig>()
                                .eq(SysConfig::getConfigKey, configKey));
        return converter.toResp(config);
    }

    /** 创建系统配置。 */
    @Override
    @Transactional
    public void create(SysConfigCreateReq req) {
        var count =
                count(
                        new LambdaQueryWrapperX<SysConfig>()
                                .eq(SysConfig::getConfigKey, req.getConfigKey()));
        if (count > 0) {
            throw new BizException(SystemErrorCode.CONFIG_KEY_EXISTS);
        }
        var entity = converter.toEntity(req);
        save(entity);
    }

    /** 更新指定系统配置。 */
    @Override
    @Transactional
    @CacheEvict(key = "'detail:id:'+#id")
    public void update(Long id, SysConfigUpdateReq req) {
        var entity = getByIdOrThrow(id);
        builtinResourceGuard.checkUpdate(entity.getIsBuiltin());
        converter.update(req, entity);
        updateById(entity);
        RedisUtil.deleteCacheKey("system:config", "detail:key:" + entity.getConfigKey());
    }

    /** 根据 ID 删除指定系统配置。 */
    @Override
    @Transactional
    @CacheEvict(key = "'detail:id:'+#id")
    public void deleteById(Long id) {
        var entity = getByIdOrThrow(id);
        checkDeletable(entity);
        removeById(id);
        RedisUtil.deleteCacheKey("system:config", "detail:key:" + entity.getConfigKey());
    }

    /** 校验系统配置是否允许删除。 */
    private void checkDeletable(SysConfig entity) {
        if (Integer.valueOf(1).equals(entity.getIsBuiltin())) {
            throw new BizException(SystemErrorCode.CONFIG_BUILTIN_NOT_DELETABLE);
        }
    }
}
