package com.travis.monolith.system.internal.service.impl;

import com.travis.monolith.system.internal.service.SysFileService;
import com.travis.monolith.system.internal.service.file.FileStorageStrategy;
import lombok.RequiredArgsConstructor;
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

    @Override
    public String upload(MultipartFile file) {
        return fileStorageStrategy.upload(file);
    }
}
