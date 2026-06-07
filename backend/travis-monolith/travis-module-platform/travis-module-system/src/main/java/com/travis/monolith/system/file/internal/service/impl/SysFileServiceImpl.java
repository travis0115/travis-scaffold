package com.travis.monolith.system.file.internal.service.impl;

import com.travis.monolith.system.file.api.SysFileService;
import com.travis.monolith.system.file.api.FileStorageStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件管理服务实现，委托给文件存储策略
 *
 * @author travis
 */
@Service
@RequiredArgsConstructor
public class SysFileServiceImpl implements SysFileService {

    private final FileStorageStrategy fileStorageStrategy;

    @Value("${travis.web.domain:}")
    private String domain;

    @Override
    public String upload(MultipartFile file) {
        return fileStorageStrategy.upload(file);
    }

    @Override
    public String getFileUrl(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        return domain != null && !domain.isEmpty() ? domain + path : path;
    }
}
