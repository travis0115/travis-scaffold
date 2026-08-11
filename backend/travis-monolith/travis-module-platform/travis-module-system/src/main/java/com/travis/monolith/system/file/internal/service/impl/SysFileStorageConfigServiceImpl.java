package com.travis.monolith.system.file.internal.service.impl;

import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.infrastructure.framework.redis.core.annotation.DistributedLock;
import com.travis.monolith.system.common.api.enums.IsDefault;
import com.travis.monolith.system.common.api.enums.Status;
import com.travis.monolith.system.common.api.enums.SystemErrorCode;
import com.travis.monolith.system.file.api.enums.FileStorageType;
import com.travis.monolith.system.file.api.request.SysFileStorageConfigCreateReq;
import com.travis.monolith.system.file.api.request.SysFileStorageConfigPageReq;
import com.travis.monolith.system.file.api.request.SysFileStorageConfigUpdateReq;
import com.travis.monolith.system.file.api.response.SysFileStorageConfigResp;
import com.travis.monolith.system.file.internal.converter.SysFileStorageConfigConverter;
import com.travis.monolith.system.file.internal.entity.SysFileStorageConfig;
import com.travis.monolith.system.file.internal.mapper.SysFileStorageConfigMapper;
import com.travis.monolith.system.file.internal.service.SysFileStorageConfigService;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 文件存储配置服务实现，负责配置查询维护、启停控制及默认配置切换。 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "system:file:storage-config")
public class SysFileStorageConfigServiceImpl
        extends ServiceImplX<SysFileStorageConfigMapper, SysFileStorageConfig>
        implements SysFileStorageConfigService {

    private final SysFileStorageConfigConverter converter;

    /** 分页查询文件存储配置。 */
    @Override
    public PageResp<SysFileStorageConfigResp> page(SysFileStorageConfigPageReq req) {
        var wrapper =
                new LambdaQueryWrapperX<SysFileStorageConfig>()
                        .orderByDesc(SysFileStorageConfig::getIsDefault)
                        .orderByAsc(SysFileStorageConfig::getCreateTime);
        return PageConverter.toResp(
                page(req.getPageNum(), req.getPageSize(), wrapper).convert(converter::toResp));
    }

    /** 查询全部文件存储配置。 */
    @Override
    @Cacheable(key = "'list:all'")
    public List<SysFileStorageConfigResp> listAll() {
        return converter.toRespList(
                list(
                        new LambdaQueryWrapperX<SysFileStorageConfig>()
                                .orderByDesc(SysFileStorageConfig::getIsDefault)
                                .orderByAsc(SysFileStorageConfig::getCreateTime)));
    }

    /** 查询指定文件存储配置，不存在时抛出业务异常。 */
    @Override
    @Cacheable(key = "'detail:' + #id")
    public SysFileStorageConfigResp getOrThrow(Long id) {
        return converter.toResp(getByIdOrThrow(id));
    }

    /** 查询默认文件存储配置，不存在时抛出业务异常。 */
    @Override
    @Cacheable(key = "'detail:default'")
    public SysFileStorageConfigResp getDefaultOrThrow() {
        return converter.toResp(
                getOneOrThrow(
                        new LambdaQueryWrapperX<SysFileStorageConfig>()
                                .eq(SysFileStorageConfig::getStatus, Status.ENABLED.getValue())
                                .eq(SysFileStorageConfig::getIsDefault, IsDefault.YES.getValue())));
    }

    /** 查询已启用的本地文件存储配置。 */
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

    /** 创建文件存储配置。 */
    @Override
    @Transactional
    @DistributedLock(namespace = "system-file-metadata", key = "'mutation'", waitTime = 5000)
    @Caching(
            evict = {
                @CacheEvict(allEntries = true),
                @CacheEvict(cacheNames = "system:user:detail", allEntries = true)
            })
    public void create(SysFileStorageConfigCreateReq req) {
        validateDefaultEnabled(req.getIsDefault(), req.getStatus());
        resetDefault(req.getIsDefault());
        save(converter.toEntity(req));
    }

    /** 更新指定文件存储配置。 */
    @Override
    @Transactional
    @DistributedLock(namespace = "system-file-metadata", key = "'mutation'", waitTime = 5000)
    @Caching(
            evict = {
                @CacheEvict(allEntries = true),
                @CacheEvict(cacheNames = "system:user:detail", allEntries = true)
            })
    public void update(Long id, SysFileStorageConfigUpdateReq req) {
        var old = getByIdOrThrow(id);
        validateDefaultEnabled(req.getIsDefault(), req.getStatus());
        if (baseMapper.existsFile(id) && isStorageLocationChanged(old, req)) {
            throw new BizException(SystemErrorCode.FILE_STORAGE_LOCATION_IMMUTABLE);
        }
        if (IsDefault.YES.getValue().equals(old.getIsDefault())
                && IsDefault.NO.getValue().equals(req.getIsDefault())) {
            throw new BizException(SystemErrorCode.FILE_STORAGE_DEFAULT_REQUIRED);
        }
        resetDefault(req.getIsDefault());
        var entity = converter.update(req, old);
        updateById(entity);
    }

    /** 更新指定文件存储配置的状态。 */
    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(allEntries = true),
                @CacheEvict(cacheNames = "system:user:detail", allEntries = true)
            })
    public void updateStatus(Long id, Integer status) {
        var entity = getByIdOrThrow(id);
        if (Status.DISABLED.getValue().equals(status)
                && IsDefault.YES.getValue().equals(entity.getIsDefault())) {
            throw new BizException(SystemErrorCode.FILE_STORAGE_DEFAULT_NOT_DELETABLE);
        }
        entity.setStatus(status);
        updateById(entity);
    }

    /** 将指定文件存储配置设为默认配置。 */
    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(allEntries = true),
                @CacheEvict(cacheNames = "system:user:detail", allEntries = true)
            })
    public void setDefault(Long id) {
        var entity = getByIdOrThrow(id);
        resetDefault(IsDefault.YES.getValue());
        entity.setIsDefault(IsDefault.YES.getValue());
        entity.setStatus(Status.ENABLED.getValue());
        updateById(entity);
    }

    /** 根据 ID 删除指定文件存储配置。 */
    @Override
    @Transactional
    @DistributedLock(namespace = "system-file-metadata", key = "'mutation'", waitTime = 5000)
    @Caching(
            evict = {
                @CacheEvict(allEntries = true),
                @CacheEvict(cacheNames = "system:user:detail", allEntries = true)
            })
    public void deleteById(Long id) {
        var entity = getByIdOrThrow(id);
        if (IsDefault.YES.getValue().equals(entity.getIsDefault())) {
            throw new BizException(SystemErrorCode.FILE_STORAGE_DEFAULT_NOT_DELETABLE);
        }
        if (baseMapper.existsFile(id)) {
            throw new BizException(SystemErrorCode.FILE_STORAGE_IN_USE);
        }
        removeById(id);
    }

    /** 按需取消原默认文件存储配置。 */
    private void resetDefault(Integer isDefault) {
        if (IsDefault.YES.getValue().equals(isDefault)) {
            lambdaUpdate()
                    .eq(SysFileStorageConfig::getIsDefault, IsDefault.YES.getValue())
                    .set(SysFileStorageConfig::getIsDefault, IsDefault.NO.getValue())
                    .update();
        }
    }

    /** 校验默认文件存储配置必须处于启用状态。 */
    private void validateDefaultEnabled(Integer isDefault, Integer status) {
        if (IsDefault.YES.getValue().equals(isDefault)
                && Status.DISABLED.getValue().equals(status)) {
            throw new BizException(SystemErrorCode.FILE_STORAGE_DEFAULT_NOT_DELETABLE);
        }
    }

    /** 判断会改变已有文件物理定位方式的配置是否发生变化。 */
    private boolean isStorageLocationChanged(
            SysFileStorageConfig old, SysFileStorageConfigUpdateReq req) {
        return !Objects.equals(old.getStorageType(), req.getStorageType())
                || !Objects.equals(old.getStoragePath(), req.getStoragePath())
                || !Objects.equals(old.getEndpoint(), req.getEndpoint())
                || !Objects.equals(old.getRegion(), req.getRegion())
                || !Objects.equals(old.getBucketId(), req.getBucketId())
                || !Objects.equals(old.getBucketName(), req.getBucketName())
                || !Objects.equals(old.getMeta(), req.getMeta());
    }
}
