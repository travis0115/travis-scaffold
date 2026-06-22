package com.travis.monolith.system.file.internal.service.impl;

import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.monolith.system.file.api.request.SysFileStorageConfigCreateReq;
import com.travis.monolith.system.file.api.request.SysFileStorageConfigUpdateReq;
import com.travis.monolith.system.file.internal.converter.SysFileStorageConfigConverter;
import com.travis.monolith.system.file.internal.entity.SysFileStorageConfig;
import com.travis.monolith.system.file.internal.mapper.SysFileStorageConfigMapper;
import com.travis.monolith.system.file.internal.service.SysFileStorageConfigService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "system:file-storage-config")
public class SysFileStorageConfigServiceImpl
        extends ServiceImplX<SysFileStorageConfigMapper, SysFileStorageConfig>
        implements SysFileStorageConfigService {

    private final SysFileStorageConfigConverter converter;

    @Override
    @Cacheable(key = "'list:all'")
    public List<SysFileStorageConfig> listAll() {
        return list();
    }

    @Override
    @Transactional
    @CacheEvict(key = "'list:all'")
    public void create(SysFileStorageConfigCreateReq req) {
        resetDefault(req.getIsDefault());
        save(converter.toEntity(req));
    }

    @Override
    @Transactional
    @CacheEvict(key = "'list:all'")
    public void update(Long id, SysFileStorageConfigUpdateReq req) {
        resetDefault(req.getIsDefault());
        var entity = converter.toEntity(req);
        entity.setId(id);
        updateById(entity);
    }

    @Override
    @Transactional
    @CacheEvict(key = "'list:all'")
    public void updateStatus(Long id, Integer status) {
        var entity = getByIdOrThrow(id);
        entity.setStatus(status);
        updateById(entity);
    }

    private void resetDefault(Integer isDefault) {
        if (Integer.valueOf(1).equals(isDefault)) {
            lambdaUpdate()
                    .eq(SysFileStorageConfig::getIsDefault, 1)
                    .set(SysFileStorageConfig::getIsDefault, 0)
                    .update();
        }
    }
}
