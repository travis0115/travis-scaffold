package com.travis.monolith.system.file.internal.service.impl;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.infrastructure.framework.web.core.event.TransactionalApplicationEventPublisher;
import com.travis.monolith.system.common.api.enums.SystemErrorCode;
import com.travis.monolith.system.file.api.SysFileReferenceChecker;
import com.travis.monolith.system.file.api.SysFileUploaderNameResolver;
import com.travis.monolith.system.file.api.event.FileDeletedEvent;
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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件管理服务实现，委托给文件存储策略
 *
 * @author travis
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SysFileServiceImpl extends ServiceImplX<SysFileMapper, SysFile>
        implements SysFileService {

    private final List<FileStorageStrategy> storageStrategies;
    private final SysFileStorageConfigService sysFileStorageConfigService;
    private final SysFileConverter converter;
    private final ObjectProvider<SysFileReferenceChecker> referenceCheckers;
    private final ObjectProvider<SysFileUploaderNameResolver> uploaderNameResolvers;
    private final TransactionalApplicationEventPublisher eventPublisher;
    private static final Map<String, SFunction<SysFile, ?>> SORT_COLUMNS =
            Map.ofEntries(
                    Map.entry("size", SysFile::getSize),
                    Map.entry("createTime", SysFile::getCreateTime));

    /** 上传文件并保存文件元数据。 */
    @Override
    public FileUploadResp upload(
            MultipartFile file, Long folderId, String uploaderType, Long uploaderId) {
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
                        .uploaderId(uploaderId)
                        .fileName(result.fileName())
                        .originalName(file.getOriginalFilename())
                        .path(result.path())
                        .mimeType(file.getContentType())
                        .size(file.getSize())
                        .extension(extension(file.getOriginalFilename()))
                        .build();
        try {
            if (!save(entity)) {
                throw new BizException(SystemErrorCode.FILE_UPLOAD_FAILED);
            }
        } catch (RuntimeException exception) {
            try {
                strategy.delete(result.path(), config);
            } catch (RuntimeException cleanupException) {
                exception.addSuppressed(cleanupException);
                log.error("文件元数据保存失败后的存储对象补偿删除失败, path={}", result.path(), cleanupException);
            }
            throw exception;
        }
        return FileUploadResp.builder()
                .id(entity.getId())
                .path(entity.getPath())
                .url(buildUrl(config.getDomain(), entity.getPath()))
                .build();
    }

    /** 分页查询文件。 */
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
        var response =
                PageConverter.toResp(
                        page.convert(
                                file ->
                                        toResp(
                                                file,
                                                storageConfigMap.get(file.getStorageConfigId()))));
        fillUploaderNames(response.getRecords());
        return response;
    }

    /** 根据文件 ID 生成可访问地址。 */
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
    public Map<Long, String> getFileUrlMapByIds(Collection<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return Map.of();
        }
        var files = listByIds(fileIds);
        if (files.isEmpty()) {
            return Map.of();
        }
        var configMap =
                sysFileStorageConfigService.listAll().stream()
                        .collect(
                                Collectors.toMap(
                                        SysFileStorageConfigResp::getId, Function.identity()));
        return files.stream()
                .collect(
                        Collectors.toMap(
                                SysFile::getId,
                                file ->
                                        buildUrl(
                                                configMap.get(file.getStorageConfigId()),
                                                file.getPath()),
                                (left, right) -> left,
                                LinkedHashMap::new));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        var file = getByIdOrThrow(id);
        if (referenceCheckers.orderedStream().anyMatch(checker -> checker.isReferenced(id))) {
            throw new BizException(SystemErrorCode.FILE_IN_USE);
        }
        var config = sysFileStorageConfigService.getOrThrow(file.getStorageConfigId());
        removeById(id);
        eventPublisher.publishEvent(
                new FileDeletedEvent(
                        config.getStorageType(), config.getStoragePath(), file.getPath()));
    }

    @Override
    public void deleteStorageObject(String storageType, String storagePath, String path) {
        var config = new SysFileStorageConfigResp();
        config.setStorageType(storageType);
        config.setStoragePath(storagePath);
        findStorageStrategy(config).delete(path, config);
    }

    /** 拼接文件访问地址。 */
    private String buildUrl(String domain, String path) {
        return domain == null || domain.isBlank() ? path : domain.replaceAll("/+$", "") + path;
    }

    /** 提取文件扩展名。 */
    private String extension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    /** 将文件实体转换并补充为响应对象。 */
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

    /** 拼接文件访问地址。 */
    private String buildUrl(SysFileStorageConfigResp config, String path) {
        return config == null ? path : buildUrl(config.getDomain(), path);
    }

    private FileStorageStrategy findStorageStrategy(SysFileStorageConfigResp config) {
        return storageStrategies.stream()
                .filter(item -> item.getStorageType().equalsIgnoreCase(config.getStorageType()))
                .findFirst()
                .orElseThrow(() -> new BizException(SystemErrorCode.FILE_STORAGE_NOT_FOUND));
    }

    private void fillUploaderNames(List<SysFileResp> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        var resolvers =
                uploaderNameResolvers
                        .orderedStream()
                        .collect(
                                Collectors.toMap(
                                        SysFileUploaderNameResolver::getUploaderType,
                                        Function.identity(),
                                        (left, right) -> left));
        records.stream()
                .filter(
                        record ->
                                record.getUploaderType() != null && record.getUploaderId() != null)
                .collect(Collectors.groupingBy(SysFileResp::getUploaderType))
                .forEach(
                        (uploaderType, typedRecords) -> {
                            var resolver = resolvers.get(uploaderType);
                            if (resolver == null) {
                                return;
                            }
                            var uploaderIds =
                                    typedRecords.stream()
                                            .map(SysFileResp::getUploaderId)
                                            .distinct()
                                            .toList();
                            var names = resolver.resolveNames(uploaderIds);
                            typedRecords.forEach(
                                    record ->
                                            record.setUploaderName(
                                                    names.get(record.getUploaderId())));
                        });
    }
}
