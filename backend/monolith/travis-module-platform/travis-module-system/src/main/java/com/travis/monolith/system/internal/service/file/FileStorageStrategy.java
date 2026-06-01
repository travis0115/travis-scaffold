package com.travis.monolith.system.internal.service.file;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储策略接口
 * 支持本地存储、OSS、MinIO等不同的存储方式
 *
 * @author travis
 */
public interface FileStorageStrategy {

    /**
     * 上传文件，返回文件访问URL
     *
     * @param file 文件
     * @return 文件访问URL
     */
    String upload(MultipartFile file);
}
