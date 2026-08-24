package com.travis.monolith.system.file.internal.strategy;

import com.travis.monolith.system.file.internal.entity.SysFileStorageConfig;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储策略接口 支持本地存储、OSS、MinIO等不同的存储方式
 *
 * @author travis
 */
public interface FileStorageStrategy {

    /** 获取存储类型 */
    String getStorageType();

    /** 上传文件 */
    StorageResult upload(MultipartFile file, SysFileStorageConfig config);

    /** 删除存储对象。 */
    void delete(String path, SysFileStorageConfig config);
}
