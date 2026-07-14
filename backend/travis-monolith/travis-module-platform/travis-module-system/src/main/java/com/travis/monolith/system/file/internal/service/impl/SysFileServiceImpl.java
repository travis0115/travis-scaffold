package com.travis.monolith.system.file.internal.service.impl;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.monolith.system.common.api.enums.SystemErrorCode;
import com.travis.monolith.system.file.api.request.SysFilePageReq;
import com.travis.monolith.system.file.api.response.FileUploadResp;
import com.travis.monolith.system.file.api.response.SysFileResp;
import com.travis.monolith.system.file.api.response.SysFileStorageConfigResp;
import com.travis.monolith.system.file.internal.converter.SysFileConverter;
import com.travis.monolith.system.file.internal.entity.SysFile;
import com.travis.monolith.system.file.internal.mapper.SysFileMapper;
import com.travis.monolith.system.file.internal.service.SysFileService;
import com.travis.monolith.system.file.internal.service.SysFileStorageConfigService;
import com.travis.monolith.system.file.internal.strategy.FileStorageStrategy;
import java.util.List;
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

    private final List<FileStorageStrategy> storageStrategies;
    private final SysFileStorageConfigService sysFileStorageConfigService;
    private final SysFileConverter converter;
    private static final Map<String, SFunction<SysFile, ?>> SORT_COLUMNS =
            Map.ofEntries(
                    Map.entry("size", SysFile::getSize),
                    Map.entry("createTime", SysFile::getCreateTime));

    @Override
    public FileUploadResp upload(
            MultipartFile file, Long folderId, String uploaderType, String uploaderName) {
        var config = sysFileStorageConfigService.getDefaultOrThrow();
        var strategy =
                storageStrategies.stream()
                        .filter(
                                item ->
                                        item.getStorageType()
                                                .equalsIgnoreCase(config.getStorageType()))
                        .findFirst()
                        .orElseThrow(
                                () -> new BizException(SystemErrorCode.FILE_STORAGE_NOT_FOUND));
        var result = strategy.upload(file, config);

        var entity =
                SysFile.builder()
                        .folderId(folderId)
                        .storageConfigId(config.getId())
                        .uploaderType(uploaderType)
                        .uploaderName(uploaderName)
                        .fileName(result.fileName())
                        .originalName(file.getOriginalFilename())
                        .path(result.path())
                        .mimeType(file.getContentType())
                        .size(file.getSize())
                        .extension(extension(file.getOriginalFilename()))
                        .build();
        save(entity);
        return FileUploadResp.builder()
                .id(entity.getId())
                .path(entity.getPath())
                .url(buildUrl(config.getDomain(), entity.getPath()))
                .build();
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
                                SORT_COLUMNS,
                                false,
                                SysFile::getCreateTime);
        if (Long.valueOf(0).equals(req.getFolderId())) {
            wrapper.and(item -> item.isNull(SysFile::getFolderId).or().eq(SysFile::getFolderId, 0));
        } else {
            wrapper.eqIfPresent(SysFile::getFolderId, req.getFolderId());
        }
        var page = page(req.getPageNum(), req.getPageSize(), wrapper);
        if (page.getRecords().isEmpty()) {
            return PageConverter.toResp(page.convert(converter::toResp));
        }
        var storageConfigMap =
                sysFileStorageConfigService.listAll().stream()
                        .collect(
                                Collectors.toMap(
                                        SysFileStorageConfigResp::getId, Function.identity()));
        return PageConverter.toResp(
                page.convert(
                        file -> toResp(file, storageConfigMap.get(file.getStorageConfigId()))));
    }

    @Override
    public String getFileUrlById(Long fileId) {
        if (fileId == null) {
            return null;
        }
        var file = getById(fileId);
        if (file == null) return null;
        var config =
                file.getStorageConfigId() == null
                        ? sysFileStorageConfigService.getDefaultOrThrow()
                        : sysFileStorageConfigService.getOrThrow(file.getStorageConfigId());
        return buildUrl(config, file.getPath());
    }

    @Override
    public void updateUploaderName(String uploaderType, Long uploaderId, String uploaderName) {
        if (uploaderType == null || uploaderType.isBlank() || uploaderId == null) {
            return;
        }
        lambdaUpdate()
                .eq(SysFile::getUploaderType, uploaderType)
                .eq(SysFile::getCreateBy, uploaderId)
                .set(SysFile::getUploaderName, uploaderName)
                .update();
    }

    private String buildUrl(String domain, String path) {
        return domain == null || domain.isBlank() ? path : domain.replaceAll("/+$", "") + path;
    }

    private String extension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private SysFileResp toResp(SysFile file, SysFileStorageConfigResp storageConfig) {
        var response = converter.toResp(file);
        response.setUrl(
                buildUrl(storageConfig == null ? null : storageConfig.getDomain(), file.getPath()));
        if (storageConfig != null) {
            response.setStorageConfigName(storageConfig.getConfigName());
            response.setStorageType(storageConfig.getStorageType());
        }
        return response;
    }

    private String buildUrl(SysFileStorageConfigResp config, String path) {
        return config == null ? path : buildUrl(config.getDomain(), path);
    }
}
