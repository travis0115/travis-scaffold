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
import com.travis.monolith.system.file.internal.mapper.SysFileMapper;
import com.travis.monolith.system.file.internal.service.SysFileFolderService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "system:file-folder")
public class SysFileFolderServiceImpl extends ServiceImplX<SysFileFolderMapper, SysFileFolder>
        implements SysFileFolderService {

    private final SysFileFolderConverter converter;
    private final SysFileMapper fileMapper;

    @Override
    @Cacheable(key = "'list:all'")
    public List<SysFileFolder> listAll() {
        return list(new LambdaQueryWrapperX<SysFileFolder>().orderByAsc(SysFileFolder::getSort));
    }

    @Override
    @CacheEvict(key = "'list:all'")
    public void create(SysFileFolderCreateReq req) {
        var entity = converter.toEntity(req);
        if (entity.getSort() == null) {
            entity.setSort(getNextSort(entity.getParentId()));
        }
        save(entity);
    }

    @Override
    @CacheEvict(key = "'list:all'")
    public void update(Long id, SysFileFolderUpdateReq req) {
        var entity = converter.toEntity(req);
        entity.setId(id);
        updateById(entity);
    }

    @Override
    @Transactional
    @CacheEvict(key = "'list:all'")
    public void deleteById(Long id) {
        var folder = getByIdOrThrow(id);
        List<SysFileFolder> folders = list();
        List<Long> folderIds = collectFolderIds(folders, id);
        boolean hasBuiltin =
                folders.stream()
                        .filter(item -> folderIds.contains(item.getId()))
                        .anyMatch(item -> IsBuiltin.BUILTIN.getValue().equals(item.getIsBuiltin()));
        if (IsBuiltin.BUILTIN.getValue().equals(folder.getIsBuiltin()) || hasBuiltin) {
            throw new BizException(SystemErrorCode.FILE_FOLDER_BUILTIN_NOT_DELETABLE);
        }
        fileMapper.update(
                null,
                new LambdaUpdateWrapper<SysFile>()
                        .in(SysFile::getFolderId, folderIds)
                        .set(SysFile::getFolderId, null));
        removeBatchByIds(folderIds);
    }

    private List<Long> collectFolderIds(List<SysFileFolder> folders, Long rootId) {
        List<Long> ids = new java.util.ArrayList<>();
        ids.add(rootId);
        for (SysFileFolder folder : folders) {
            if (rootId.equals(folder.getParentId())) {
                ids.addAll(collectFolderIds(folders, folder.getId()));
            }
        }
        return ids;
    }

    private Integer getNextSort(Long parentId) {
        Integer maxSort =
                getBaseMapper()
                        .selectObjs(
                                new LambdaQueryWrapperX<SysFileFolder>()
                                        .select(SysFileFolder::getSort)
                                        .eq(
                                                SysFileFolder::getParentId,
                                                parentId == null ? 0 : parentId)
                                        .orderByDesc(SysFileFolder::getSort)
                                        .last("LIMIT 1"))
                        .stream()
                        .findFirst()
                        .map(item -> (Integer) item)
                        .orElse(null);
        return maxSort == null ? 0 : maxSort + 1;
    }
}
