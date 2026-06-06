package com.travis.monolith.system.internal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.framework.web.core.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.common.web.model.PageResult;
import com.travis.monolith.system.internal.converter.SysConfigConverter;
import com.travis.monolith.system.internal.mapper.SysConfigMapper;
import com.travis.monolith.system.internal.model.entity.SysConfig;
import com.travis.monolith.system.internal.model.request.config.SysConfigPageReq;
import com.travis.monolith.system.internal.model.request.config.SysConfigReq;
import com.travis.monolith.system.internal.model.response.config.SysConfigResp;
import com.travis.monolith.system.internal.service.SysConfigService;
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
    public PageResult<SysConfigResp> getConfigPage(SysConfigPageReq req) {
        LambdaQueryWrapper<SysConfig> wrapper =
                new LambdaQueryWrapper<SysConfig>()
                        .like(
                                req.getConfigGroup() != null,
                                SysConfig::getConfigGroup,
                                req.getConfigGroup())
                        .like(
                                req.getConfigKey() != null,
                                SysConfig::getConfigKey,
                                req.getConfigKey())
                        .orderByAsc(SysConfig::getConfigGroup, SysConfig::getConfigKey);
        Page<SysConfig> page = page(new Page<>(req.getPageNum(), req.getPageSize()), wrapper);
        return new PageResult<>(
                converter.toRespList(page.getRecords()),
                page.getTotal(),
                (int) page.getCurrent(),
                (int) page.getSize(),
                (int) page.getPages());
    }

    @Override
    public SysConfigResp getConfigDetail(Long id) {
        SysConfig config = getById(id);
        if (config == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        return converter.toResp(config);
    }

    @Override
    public String getConfigValue(String configKey) {
        SysConfig config =
                getOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, configKey));
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    @Transactional
    public void addConfig(SysConfigReq req) {
        SysConfig entity = converter.toEntity(req);
        save(entity);
    }

    @Override
    @Transactional
    public void updateConfig(Long id, SysConfigReq req) {
        SysConfig entity = getById(id);
        if (entity == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        converter.update(req, entity);
        updateById(entity);
    }

    @Override
    @Transactional
    public void deleteConfig(Long id) {
        removeById(id);
    }
}
