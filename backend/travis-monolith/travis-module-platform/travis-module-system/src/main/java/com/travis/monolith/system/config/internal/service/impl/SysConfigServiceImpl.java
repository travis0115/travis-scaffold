package com.travis.monolith.system.config.internal.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.common.web.model.PageResult;
import com.travis.infrastructure.framework.web.core.exception.BizException;
import com.travis.monolith.system.config.api.request.SysConfigPageReq;
import com.travis.monolith.system.config.api.request.SysConfigReq;
import com.travis.monolith.system.config.api.response.SysConfigResp;
import com.travis.monolith.system.config.internal.converter.SysConfigConverter;
import com.travis.monolith.system.config.internal.entity.SysConfig;
import com.travis.monolith.system.config.internal.mapper.SysConfigMapper;
import com.travis.monolith.system.config.internal.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统配置服务实现
 *
 * @author travis
 */
@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig>
        implements SysConfigService {

    private final SysConfigConverter converter;

    @Override
    public PageResult<SysConfigResp> page(SysConfigPageReq req) {
        var wrapper =
                new LambdaQueryWrapper<SysConfig>()
                        .like(
                                StrUtil.isNotBlank(req.getConfigGroup()),
                                SysConfig::getConfigGroup,
                                req.getConfigGroup())
                        .like(
                                StrUtil.isNotBlank(req.getConfigKey()),
                                SysConfig::getConfigKey,
                                req.getConfigKey())
                        .orderByAsc(SysConfig::getConfigGroup, SysConfig::getConfigKey);
        Page<SysConfig> page = page(new Page<>(req.getPageNum(), req.getPageSize()), wrapper);
        return new PageResult<>(
                converter.toRespList(page.getRecords()),
                page.getTotal(),
                page.getCurrent(),
                page.getSize(),
                page.getPages());
    }

    @Override
    public SysConfigResp getById(Long id) {
        SysConfig config = super.getById(id);
        if (config == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        return converter.toResp(config);
    }

    @Override
    public String getValue(String configKey) {
        SysConfig config =
                getOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, configKey));
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    @Transactional
    public void create(SysConfigReq req) {
        SysConfig entity = converter.toEntity(req);
        save(entity);
    }

    @Override
    @Transactional
    public void update(Long id, SysConfigReq req) {
        SysConfig entity = super.getById(id);
        if (entity == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        converter.update(req, entity);
        updateById(entity);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        removeById(id);
    }
}
