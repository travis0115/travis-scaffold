package com.travis.monolith.system.file.internal.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.monolith.system.file.api.request.SysFilePageReq;
import com.travis.monolith.system.file.api.response.FileUploadResp;
import com.travis.monolith.system.file.api.response.SysFileResp;
import com.travis.monolith.system.file.internal.entity.SysFile;
import com.travis.monolith.system.file.internal.entity.SysFileStorageConfig;
import com.travis.monolith.system.file.internal.mapper.SysFileMapper;
import com.travis.monolith.system.file.internal.mapper.SysFileStorageConfigMapper;
import com.travis.monolith.system.file.internal.service.FileStorageStrategy;
import com.travis.monolith.system.file.internal.service.SysFileService;
import com.travis.monolith.system.user.api.SysUserApi;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
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
    private final SysUserApi userApi;

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
    public PageResp<SysFileResp> page(SysFilePageReq req) {
        var wrapper =
                new LambdaQueryWrapperX<SysFile>()
                        .eqIfPresent(SysFile::getFolderId, req.getFolderId())
                        .likeIfPresent(SysFile::getOriginalName, req.getFileName())
                        .likeIfPresent(SysFile::getMimeType, req.getMimeType())
                        .eqIfPresent(SysFile::getStorageConfigId, req.getStorageConfigId())
                        .orderByDesc(SysFile::getCreateTime);
        Page<SysFile> page = page(new Page<>(req.getPageNum(), req.getPageSize()), wrapper);
        if (page.getRecords().isEmpty()) {
            return PageConverter.toResp(
                    new Page<SysFileResp>(page.getCurrent(), page.getSize(), page.getTotal()));
        }
        Map<Long, SysFileStorageConfig> storageConfigMap =
                storageConfigMapper
                        .selectBatchIds(
                                page.getRecords().stream()
                                        .map(SysFile::getStorageConfigId)
                                        .distinct()
                                        .toList())
                        .stream()
                        .collect(
                                Collectors.toMap(SysFileStorageConfig::getId, Function.identity()));
        Map<Long, String> usernameMap =
                userApi.getUsernameMapByIds(
                        page.getRecords().stream()
                                .map(SysFile::getCreateBy)
                                .filter(java.util.Objects::nonNull)
                                .distinct()
                                .toList());
        return PageConverter.toResp(
                page.convert(
                        file ->
                                toResponse(
                                        file,
                                        storageConfigMap.get(file.getStorageConfigId()),
                                        usernameMap.get(file.getCreateBy()))));
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

    private SysFileResp toResponse(
            SysFile file, SysFileStorageConfig storageConfig, String creatorName) {
        var response = new SysFileResp();
        response.setId(file.getId());
        response.setFolderId(file.getFolderId());
        response.setStorageConfigId(file.getStorageConfigId());
        response.setFileName(file.getFileName());
        response.setOriginalName(file.getOriginalName());
        response.setPath(file.getPath());
        response.setUrl(file.getUrl());
        response.setExtension(file.getExtension());
        response.setMimeType(file.getMimeType());
        response.setSize(file.getSize());
        response.setCreateBy(file.getCreateBy());
        response.setCreatorName(creatorName);
        response.setCreateTime(file.getCreateTime());
        if (storageConfig != null) {
            response.setStorageConfigName(storageConfig.getConfigName());
            response.setStorageType(storageConfig.getStorageType());
        }
        return response;
    }
}
