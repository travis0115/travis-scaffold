package com.travis.monolith.system.file.internal.service.impl;

import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.monolith.system.file.api.request.SysFileFolderCreateReq;
import com.travis.monolith.system.file.api.request.SysFileFolderUpdateReq;
import com.travis.monolith.system.file.internal.converter.SysFileFolderConverter;
import com.travis.monolith.system.file.internal.entity.SysFileFolder;
import com.travis.monolith.system.file.internal.mapper.SysFileFolderMapper;
import com.travis.monolith.system.file.internal.service.SysFileFolderService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "system:file-folder")
public class SysFileFolderServiceImpl extends ServiceImplX<SysFileFolderMapper, SysFileFolder>
        implements SysFileFolderService {

    private final SysFileFolderConverter converter;

    @Override
    @Cacheable(key = "'list:all'")
    public List<SysFileFolder> listAll() {
        return list(new LambdaQueryWrapperX<SysFileFolder>().orderByAsc(SysFileFolder::getSort));
    }

    @Override
    @CacheEvict(key = "'list:all'")
    public void create(SysFileFolderCreateReq req) {
        save(converter.toEntity(req));
    }

    @Override
    @CacheEvict(key = "'list:all'")
    public void update(Long id, SysFileFolderUpdateReq req) {
        var entity = converter.toEntity(req);
        entity.setId(id);
        updateById(entity);
    }
}
