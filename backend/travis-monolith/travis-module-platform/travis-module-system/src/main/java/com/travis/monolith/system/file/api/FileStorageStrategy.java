package com.travis.monolith.system.file.api;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储策略接口 支持本地存储、OSS、MinIO等不同的存储方式
 *
 * @author travis
 */
public interface FileStorageStrategy {

    /**
     * 上传文件，返回文件相对路径（不含域名）
     *
     * @param file 文件
     * @return 文件相对路径，如 /files/2026-06-02/abc.jpg
     */
    String upload(MultipartFile file);
}
