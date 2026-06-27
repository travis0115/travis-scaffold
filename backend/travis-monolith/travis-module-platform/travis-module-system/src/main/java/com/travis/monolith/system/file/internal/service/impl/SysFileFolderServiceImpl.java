package com.travis.monolith.system.file.internal.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.monolith.system.common.api.enums.IsBuiltin;
import com.travis.monolith.system.common.api.enums.SystemErrorCode;
import com.travis.monolith.system.file.api.request.SysFileFolderCreateReq;
import com.travis.monolith.system.file.api.request.SysFileFolderUpdateReq;
import com.travis.monolith.system.file.internal.converter.SysFileFolderConverter;
import com.travis.monolith.system.file.internal.entity.SysFile;
import com.travis.monolith.system.file.internal.entity.SysFileFolder;
import com.travis.monolith.system.file.internal.mapper.SysFileFolderMapper;
import com.travis.monolith.system.file.internal.service.SysFileFolderService;
import com.travis.monolith.system.file.internal.service.SysFileService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "system:file:folder")
public class SysFileFolderServiceImpl extends ServiceImplX<SysFileFolderMapper, SysFileFolder>
        implements SysFileFolderService {

    private final SysFileFolderConverter converter;
    private final SysFileService sysFileService;

    @Override
    @Cacheable(key = "'list:all'")
    public List<SysFileFolder> listAll() {
        return list(
                new LambdaQueryWrapperX<SysFileFolder>()
                        .orderByDesc(SysFileFolder::getIsBuiltin)
                        .orderByAsc(SysFileFolder::getSort)
                        .orderByAsc(SysFileFolder::getCreateTime));
    }

    @Override
    @CacheEvict(key = "'list:all'")
    public void create(SysFileFolderCreateReq req) {
        save(converter.toEntity(req));
    }

    @Override
    @CacheEvict(key = "'list:all'")
    public void update(Long id, SysFileFolderUpdateReq req) {
        var folder = getByIdOrThrow(id);
        converter.update(req, folder);
        updateById(folder);
    }

    @Override
    @Transactional
    @CacheEvict(key = "'list:all'")
    public void deleteById(Long id) {
        var folder = getByIdOrThrow(id);
        var folders = list();
        var folderIds = collectFolderIds(folders, id);
        boolean hasBuiltin =
                folders.stream()
                        .filter(item -> folderIds.contains(item.getId()))
                        .anyMatch(item -> IsBuiltin.YES.getValue().equals(item.getIsBuiltin()));
        if (IsBuiltin.YES.getValue().equals(folder.getIsBuiltin()) || hasBuiltin) {
            throw new BizException(SystemErrorCode.FILE_FOLDER_BUILTIN_NOT_DELETABLE);
        }

        sysFileService.update(
                new LambdaUpdateWrapper<SysFile>()
                        .in(SysFile::getFolderId, folderIds)
                        .set(SysFile::getFolderId, null));
        removeBatchByIds(folderIds);
    }

    private List<Long> collectFolderIds(List<SysFileFolder> folders, Long rootId) {
        var ids = new ArrayList<Long>();
        ids.add(rootId);
        for (SysFileFolder folder : folders) {
            if (rootId.equals(folder.getParentId())) {
                ids.addAll(collectFolderIds(folders, folder.getId()));
            }
        }
        return ids;
    }
}
