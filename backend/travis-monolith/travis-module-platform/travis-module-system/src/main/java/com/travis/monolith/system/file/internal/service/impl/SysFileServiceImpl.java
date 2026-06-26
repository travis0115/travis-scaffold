package com.travis.monolith.system.file.internal.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.monolith.system.file.api.request.SysFilePageReq;
import com.travis.monolith.system.file.api.response.FileUploadResp;
import com.travis.monolith.system.file.api.response.SysFileResp;
import com.travis.monolith.system.file.internal.entity.SysFile;
import com.travis.monolith.system.file.internal.entity.SysFileStorageConfig;
import com.travis.monolith.system.file.internal.mapper.SysFileMapper;
import com.travis.monolith.system.file.internal.mapper.SysFileStorageConfigMapper;
import com.travis.monolith.system.file.internal.service.FileStorageStrategy;
import com.travis.monolith.system.file.internal.service.SysFileService;
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
public class SysFileServiceImpl extends ServiceImplX<SysFileMapper, SysFile>
        implements SysFileService {

    private final java.util.List<FileStorageStrategy> storageStrategies;
    private final SysFileStorageConfigMapper storageConfigMapper;

    @Override
    public FileUploadResp upload(MultipartFile file, Long folderId) {
        var config =
                storageConfigMapper.selectOne(
                        new LambdaQueryWrapperX<SysFileStorageConfig>()
                                .eq(SysFileStorageConfig::getIsDefault, 1)
                                .eq(SysFileStorageConfig::getStatus, 1)
                                .last("LIMIT 1"));
        if (config == null) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
        var strategy =
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
        entity.setMimeType(file.getContentType());
        entity.setSize(file.getSize());
        entity.setExtension(extension(file.getOriginalFilename()));
        save(entity);
        var response =
                new FileUploadResp(
                        entity.getPath(), buildUrl(config.getDomain(), entity.getPath()));
        response.setId(entity.getId());
        return response;
    }

    @Override
    public PageResp<SysFileResp> page(SysFilePageReq req) {
        var wrapper =
                new LambdaQueryWrapperX<SysFile>()
                        .likeIfPresent(SysFile::getFileName, req.getFileName())
                        .likeIfPresent(SysFile::getOriginalName, req.getOriginalName())
                        .likeIfPresent(SysFile::getMimeType, req.getMimeType())
                        .eqIfPresent(SysFile::getStorageConfigId, req.getStorageConfigId())
                        .orderByAllowed(
                                req.getOrderBy(),
                                req.getAsc(),
                                Map.of(
                                        "size",
                                        SysFile::getSize,
                                        "createTime",
                                        SysFile::getCreateTime),
                                false,
                                SysFile::getCreateTime);
        if (Boolean.TRUE.equals(req.getUnclassified())
                || Long.valueOf(0).equals(req.getFolderId())) {
            wrapper.and(item -> item.isNull(SysFile::getFolderId).or().eq(SysFile::getFolderId, 0));
        } else {
            wrapper.eqIfPresent(SysFile::getFolderId, req.getFolderId());
        }
        Page<SysFile> page = page(req.getPageNum(), req.getPageSize(), wrapper);
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
        return PageConverter.toResp(
                page.convert(
                        file -> toResponse(file, storageConfigMap.get(file.getStorageConfigId()))));
    }

    @Override
    public String getFileUrl(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        if (isAbsoluteUrl(path)) {
            return path;
        }
        var config = getDefaultStorageConfig();
        return config == null ? path : buildUrl(config.getDomain(), path);
    }

    @Override
    public String getFileUrlById(Long fileId) {
        if (fileId == null) {
            return null;
        }
        var file = getById(fileId);
        if (file == null) {
            return null;
        }
        var config =
                file.getStorageConfigId() == null
                        ? getDefaultStorageConfig()
                        : storageConfigMapper.selectById(file.getStorageConfigId());
        return config == null ? file.getPath() : buildUrl(config.getDomain(), file.getPath());
    }

    private SysFileStorageConfig getDefaultStorageConfig() {
        return storageConfigMapper.selectOne(
                new LambdaQueryWrapperX<SysFileStorageConfig>()
                        .eq(SysFileStorageConfig::getIsDefault, 1)
                        .eq(SysFileStorageConfig::getStatus, 1)
                        .last("LIMIT 1"));
    }

    private boolean isAbsoluteUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://") || url.startsWith("//");
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

    private SysFileResp toResponse(SysFile file, SysFileStorageConfig storageConfig) {
        var response = new SysFileResp();
        response.setId(file.getId());
        response.setFolderId(file.getFolderId());
        response.setStorageConfigId(file.getStorageConfigId());
        response.setFileName(file.getFileName());
        response.setOriginalName(file.getOriginalName());
        response.setPath(file.getPath());
        response.setUrl(
                storageConfig == null
                        ? file.getPath()
                        : buildUrl(storageConfig.getDomain(), file.getPath()));
        response.setExtension(file.getExtension());
        response.setMimeType(file.getMimeType());
        response.setSize(file.getSize());
        response.setCreateTime(file.getCreateTime());
        if (storageConfig != null) {
            response.setStorageConfigName(storageConfig.getConfigName());
            response.setStorageType(storageConfig.getStorageType());
        }
        return response;
    }
}
