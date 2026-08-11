package com.travis.monolith.system.file.internal.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.infrastructure.framework.redis.core.annotation.DistributedLock;
import com.travis.infrastructure.framework.redis.core.annotation.DistributedLockNamespace;
import com.travis.monolith.system.common.api.BuiltinResourceGuard;
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

/** 文件夹管理服务实现，负责文件夹查询、创建、更新及删除校验。 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "system:file:folder")
@DistributedLockNamespace("system-file-metadata")
public class SysFileFolderServiceImpl extends ServiceImplX<SysFileFolderMapper, SysFileFolder>
        implements SysFileFolderService {

    private final SysFileFolderConverter converter;
    private final SysFileService sysFileService;
    private final BuiltinResourceGuard builtinResourceGuard;

    /** 查询全部文件夹。 */
    @Override
    @Cacheable(key = "'list:all'")
    public List<SysFileFolder> listAll() {
        return list(
                new LambdaQueryWrapperX<SysFileFolder>()
                        .orderByDesc(SysFileFolder::getIsBuiltin)
                        .orderByAsc(SysFileFolder::getSort)
                        .orderByAsc(SysFileFolder::getCreateTime));
    }

    /** 创建文件夹。 */
    @Override
    @Transactional
    @DistributedLock(key = "'mutation'", waitTime = 5000)
    @CacheEvict(key = "'list:all'")
    public void create(SysFileFolderCreateReq req) {
        if (!Long.valueOf(0L).equals(req.getParentId()) && getById(req.getParentId()) == null) {
            throw new BizException(SystemErrorCode.FILE_FOLDER_PARENT_INVALID);
        }
        save(converter.toEntity(req));
    }

    /** 更新指定文件夹。 */
    @Override
    @Transactional
    @DistributedLock(key = "'mutation'", waitTime = 5000)
    @CacheEvict(key = "'list:all'")
    public void update(Long id, SysFileFolderUpdateReq req) {
        var folder = getByIdOrThrow(id);
        builtinResourceGuard.checkUpdate(folder.getIsBuiltin());
        converter.update(req, folder);
        updateById(folder);
    }

    /** 根据 ID 删除指定文件夹。 */
    @Override
    @Transactional
    @DistributedLock(key = "'mutation'", waitTime = 5000)
    @CacheEvict(key = "'list:all'")
    public void deleteById(Long id) {
        var folder = getById(id);
        if (folder == null) {
            throw new BizException(CommonErrorCode.DATABASE_RECORD_NOT_FOUND);
        }
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

    /** 收集指定文件夹及其全部下级文件夹 ID。 */
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
