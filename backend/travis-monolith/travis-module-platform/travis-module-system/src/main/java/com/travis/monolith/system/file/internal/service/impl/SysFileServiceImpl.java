package com.travis.monolith.system.file.internal.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.web.core.exception.BizException;
import com.travis.monolith.system.file.api.request.SysFilePageReq;
import com.travis.monolith.system.file.api.response.FileUploadResp;
import com.travis.monolith.system.file.internal.entity.SysFile;
import com.travis.monolith.system.file.internal.entity.SysFileStorageConfig;
import com.travis.monolith.system.file.internal.mapper.SysFileMapper;
import com.travis.monolith.system.file.internal.mapper.SysFileStorageConfigMapper;
import com.travis.monolith.system.file.internal.service.FileStorageStrategy;
import com.travis.monolith.system.file.internal.service.SysFileService;
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
public class SysFileServiceImpl extends ServiceImpl<SysFileMapper, SysFile>
        implements SysFileService {

    private final java.util.List<FileStorageStrategy> storageStrategies;
    private final SysFileStorageConfigMapper storageConfigMapper;

    @Override
    public FileUploadResp upload(MultipartFile file, Long folderId) {
        SysFileStorageConfig config =
                storageConfigMapper.selectOne(
                        new LambdaQueryWrapperX<SysFileStorageConfig>()
                                .eq(SysFileStorageConfig::getIsDefault, 1)
                                .eq(SysFileStorageConfig::getStatus, 1)
                                .last("LIMIT 1"));
        if (config == null) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
        FileStorageStrategy strategy =
                storageStrategies.stream()
                        .filter(
                                item ->
                                        item.getStorageType()
                                                .equalsIgnoreCase(config.getStorageType()))
                        .findFirst()
                        .orElseThrow(() -> new BizException(CommonErrorCode.BAD_REQUEST));
        var result = strategy.upload(file, config);
        var entity = new SysFile();
        entity.setFolderId(folderId);
        entity.setStorageConfigId(config.getId());
        entity.setFileName(result.fileName());
        entity.setOriginalName(file.getOriginalFilename());
        entity.setPath(result.path());
        entity.setUrl(buildUrl(config.getDomain(), result.path()));
        entity.setMimeType(file.getContentType());
        entity.setSize(file.getSize());
        entity.setExtension(extension(file.getOriginalFilename()));
        save(entity);
        var response = new FileUploadResp(entity.getPath(), entity.getUrl());
        response.setId(entity.getId());
        return response;
    }

    @Override
    public PageResp<SysFile> page(SysFilePageReq req) {
        var wrapper =
                new LambdaQueryWrapperX<SysFile>()
                        .eqIfPresent(SysFile::getFolderId, req.getFolderId())
                        .likeIfPresent(SysFile::getOriginalName, req.getFileName())
                        .likeIfPresent(SysFile::getMimeType, req.getMimeType())
                        .eqIfPresent(SysFile::getStorageConfigId, req.getStorageConfigId())
                        .orderByDesc(SysFile::getCreateTime);
        return PageConverter.toResp(page(new Page<>(req.getPageNum(), req.getPageSize()), wrapper));
    }

    @Override
    public String getFileUrl(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        return path;
    }

    private String buildUrl(String domain, String path) {
        return domain == null || domain.isBlank() ? path : domain.replaceAll("/+$", "") + path;
    }

    private String extension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
