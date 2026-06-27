package com.travis.monolith.system.file.internal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.monolith.system.common.api.enums.IsDefault;
import com.travis.monolith.system.common.api.enums.Status;
import com.travis.monolith.system.common.api.enums.SystemErrorCode;
import com.travis.monolith.system.file.api.enums.FileStorageType;
import com.travis.monolith.system.file.api.request.SysFileStorageConfigCreateReq;
import com.travis.monolith.system.file.api.request.SysFileStorageConfigUpdateReq;
import com.travis.monolith.system.file.api.response.SysFileStorageConfigResp;
import com.travis.monolith.system.file.internal.converter.SysFileStorageConfigConverter;
import com.travis.monolith.system.file.internal.entity.SysFile;
import com.travis.monolith.system.file.internal.entity.SysFileStorageConfig;
import com.travis.monolith.system.file.internal.mapper.SysFileStorageConfigMapper;
import com.travis.monolith.system.file.internal.service.SysFileService;
import com.travis.monolith.system.file.internal.service.SysFileStorageConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "system:file:storage-config")
public class SysFileStorageConfigServiceImpl
        extends ServiceImplX<SysFileStorageConfigMapper, SysFileStorageConfig>
        implements SysFileStorageConfigService {

    private final SysFileStorageConfigConverter converter;
    private final SysFileService sysFileService;

    @Override
    @Cacheable(key = "'list:all'")
    public List<SysFileStorageConfigResp> listAll() {
        return converter.toRespList(
                list(
                        new LambdaQueryWrapperX<SysFileStorageConfig>()
                                .orderByDesc(SysFileStorageConfig::getIsDefault)
                                .orderByAsc(SysFileStorageConfig::getCreateTime)));
    }

    @Override
    @Cacheable(key = "'detail:' + #id")
    public SysFileStorageConfigResp get(Long id) {
        return converter.toResp(getByIdOrThrow(id));
    }

    @Override
    @Cacheable(key = "'detail:default'")
    public SysFileStorageConfigResp getDefault() {
        return converter.toResp(
                getOneOrThrow(
                        new LambdaQueryWrapperX<SysFileStorageConfig>()
                                .eq(SysFileStorageConfig::getStatus, Status.ENABLED.getValue())
                                .eq(SysFileStorageConfig::getIsDefault, IsDefault.YES.getValue())));
    }

    @Override
    @Cacheable(key = "'list:local-enabled'")
    public List<SysFileStorageConfig> listEnabledLocalConfigs() {
        return list(
                new LambdaQueryWrapperX<SysFileStorageConfig>()
                        .eq(SysFileStorageConfig::getStorageType, FileStorageType.LOCAL.getValue())
                        .eq(SysFileStorageConfig::getStatus, Status.ENABLED.getValue())
                        .orderByDesc(SysFileStorageConfig::getIsDefault)
                        .orderByAsc(SysFileStorageConfig::getCreateTime));
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void create(SysFileStorageConfigCreateReq req) {
        resetDefault(req.getIsDefault());
        save(converter.toEntity(req));
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void update(Long id, SysFileStorageConfigUpdateReq req) {
        var old = getByIdOrThrow(id);
        if (IsDefault.YES.getValue().equals(old.getIsDefault())
                && IsDefault.NO.getValue().equals(req.getIsDefault())) {
            throw new BizException(SystemErrorCode.FILE_STORAGE_DEFAULT_REQUIRED);
        }
        resetDefault(req.getIsDefault());
        var entity = converter.update(req, old);
        updateById(entity);
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void updateStatus(Long id, Integer status) {
        var entity = getByIdOrThrow(id);
        if (Status.DISABLED.getValue().equals(status)
                && IsDefault.YES.getValue().equals(entity.getIsDefault())) {
            throw new BizException(SystemErrorCode.FILE_STORAGE_DEFAULT_NOT_DELETABLE);
        }
        entity.setStatus(status);
        updateById(entity);
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void setDefault(Long id) {
        var entity = getByIdOrThrow(id);
        resetDefault(IsDefault.YES.getValue());
        entity.setIsDefault(IsDefault.YES.getValue());
        entity.setStatus(Status.ENABLED.getValue());
        updateById(entity);
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void deleteById(Long id) {
        var entity = getByIdOrThrow(id);
        if (IsDefault.YES.getValue().equals(entity.getIsDefault())) {
            throw new BizException(SystemErrorCode.FILE_STORAGE_DEFAULT_NOT_DELETABLE);
        }
        var fileCount =
                sysFileService.count(
                        new LambdaQueryWrapper<SysFile>().eq(SysFile::getStorageConfigId, id));
        if (fileCount > 0) {
            throw new BizException(SystemErrorCode.FILE_STORAGE_IN_USE);
        }
        removeById(id);
    }

    private void resetDefault(Integer isDefault) {
        if (IsDefault.YES.getValue().equals(isDefault)) {
            lambdaUpdate()
                    .eq(SysFileStorageConfig::getIsDefault, IsDefault.YES.getValue())
                    .set(SysFileStorageConfig::getIsDefault, IsDefault.NO.getValue())
                    .update();
        }
    }
}
