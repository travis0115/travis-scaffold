package com.travis.monolith.system.file.internal.service.impl;

import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.framework.redis.core.annotation.DistributedLock;
import com.travis.monolith.system.common.api.enums.SystemErrorCode;
import com.travis.monolith.system.file.internal.entity.SysFile;
import com.travis.monolith.system.file.internal.entity.SysFileStorageConfig;
import com.travis.monolith.system.file.internal.mapper.SysFileFolderMapper;
import com.travis.monolith.system.file.internal.mapper.SysFileMapper;
import com.travis.monolith.system.file.internal.mapper.SysFileStorageConfigMapper;
import com.travis.monolith.system.file.internal.service.SysFileMetadataService;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 文件元数据写入服务，保证文件夹校验与元数据保存不被树结构变更打断。 */
@Service
@RequiredArgsConstructor
public class SysFileMetadataServiceImpl implements SysFileMetadataService {

    private final SysFileMapper fileMapper;
    private final SysFileFolderMapper folderMapper;
    private final SysFileStorageConfigMapper storageConfigMapper;

    /** 校验目标文件夹并保存文件元数据。 */
    @Transactional
    @DistributedLock(namespace = "system-file-metadata", key = "'mutation'", waitTime = 5000)
    @Override
    public void save(SysFile file, SysFileStorageConfig configSnapshot) {
        var folderId = file.getFolderId();
        if (folderId != null && folderId != 0L && folderMapper.selectById(folderId) == null) {
            throw new BizException(SystemErrorCode.FILE_FOLDER_PARENT_INVALID);
        }
        var currentConfig = storageConfigMapper.selectById(file.getStorageConfigId());
        if (currentConfig == null || isStorageLocationChanged(currentConfig, configSnapshot)) {
            throw new BizException(SystemErrorCode.FILE_STORAGE_LOCATION_IMMUTABLE);
        }
        if (fileMapper.insert(file) != 1) {
            throw new BizException(SystemErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private boolean isStorageLocationChanged(
            SysFileStorageConfig current, SysFileStorageConfig snapshot) {
        return !Objects.equals(current.getStorageType(), snapshot.getStorageType())
                || !Objects.equals(current.getStoragePath(), snapshot.getStoragePath())
                || !Objects.equals(current.getEndpoint(), snapshot.getEndpoint())
                || !Objects.equals(current.getRegion(), snapshot.getRegion())
                || !Objects.equals(current.getBucketId(), snapshot.getBucketId())
                || !Objects.equals(current.getBucketName(), snapshot.getBucketName())
                || !Objects.equals(current.getMeta(), snapshot.getMeta());
    }
}
