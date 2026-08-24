package com.travis.monolith.system.file.internal.strategy;

import cn.hutool.core.util.StrUtil;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.monolith.system.common.api.enums.SystemErrorCode;
import com.travis.monolith.system.file.api.enums.FileStorageType;
import com.travis.monolith.system.file.internal.config.properties.FileUploadProperties;
import com.travis.monolith.system.file.internal.entity.SysFileStorageConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 本地文件存储策略 将文件保存到服务器本地磁盘，通过静态资源映射提供访问
 *
 * @author travis
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LocalFileStorageStrategy implements FileStorageStrategy {

    /** 本地文件按日期分目录存储时使用的路径格式。 */
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final Environment environment;
    private final FileUploadProperties fileUploadProperties;

    @Override
    public String getStorageType() {
        return FileStorageType.LOCAL.getValue();
    }

    @Override
    public StorageResult upload(MultipartFile file, SysFileStorageConfig config) {
        if (file == null || file.isEmpty()) {
            throw new BizException(SystemErrorCode.FILE_UPLOAD_FAILED);
        }

        var originalFilename = file.getOriginalFilename();

        // 按日期分目录，UUID生成文件名
        var datePath = LocalDate.now().format(DATE_FORMAT);
        var filename = generateFilename(originalFilename);

        // 创建目标目录
        var storagePath =
                config == null || StrUtil.isBlank(config.getStoragePath())
                        ? null
                        : config.getStoragePath();
        if (storagePath == null) {
            throw new BizException(SystemErrorCode.FILE_UPLOAD_FAILED);
        }
        var dirPath = Paths.get(environment.resolvePlaceholders(storagePath), datePath);
        try {
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            log.error("创建上传目录失败: {}", dirPath, e);
            throw new BizException(SystemErrorCode.FILE_UPLOAD_FAILED);
        }

        // 保存文件
        var filePath = dirPath.resolve(filename);
        try {
            file.transferTo(filePath.toFile());
        } catch (IOException e) {
            log.error("文件保存失败: {}", filePath, e);
            throw new BizException(SystemErrorCode.FILE_UPLOAD_FAILED);
        }

        var resourceHandler = fileUploadProperties.getResourceHandler();
        if (StrUtil.isBlank(resourceHandler)) {
            resourceHandler = "";
        }
        String urlPrefix = resourceHandler.replaceAll("/\\*\\*$", "").replaceAll("/+$", "");
        return new StorageResult(urlPrefix + "/" + datePath + "/" + filename, filename);
    }

    @Override
    public void delete(String path, SysFileStorageConfig config) {
        if (StrUtil.isBlank(path) || config == null || StrUtil.isBlank(config.getStoragePath())) {
            return;
        }
        var resourceHandler = fileUploadProperties.getResourceHandler();
        var urlPrefix =
                StrUtil.blankToDefault(resourceHandler, "")
                        .replaceAll("/\\*\\*$", "")
                        .replaceAll("^/+|/+$", "");
        var normalizedPath = path.replaceFirst("^/+", "");
        if (!urlPrefix.isBlank() && normalizedPath.startsWith(urlPrefix + "/")) {
            normalizedPath = normalizedPath.substring(urlPrefix.length() + 1);
        }
        Path storageRoot =
                Paths.get(environment.resolvePlaceholders(config.getStoragePath()))
                        .toAbsolutePath()
                        .normalize();
        Path filePath = storageRoot.resolve(normalizedPath).normalize();
        if (!filePath.startsWith(storageRoot)) {
            throw new BizException(SystemErrorCode.FILE_UPLOAD_FAILED);
        }
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException exception) {
            log.error("删除存储文件失败: {}", filePath, exception);
            throw new BizException(SystemErrorCode.FILE_UPLOAD_FAILED, exception);
        }
    }

    private String generateFilename(String originalFilename) {
        return UUID.randomUUID().toString().replace("-", "") + getSafeExtension(originalFilename);
    }

    private String getSafeExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BizException(SystemErrorCode.FILE_UPLOAD_EXTENSION_NOT_ALLOWED);
        }
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex <= 0 || dotIndex == originalFilename.length() - 1) {
            throw new BizException(SystemErrorCode.FILE_UPLOAD_EXTENSION_NOT_ALLOWED);
        }
        String extension = originalFilename.substring(dotIndex + 1).toLowerCase();
        if (!extension.matches("[a-z0-9]{1,20}")
                || !fileUploadProperties.getNormalizedAllowedExtensions().contains(extension)) {
            throw new BizException(SystemErrorCode.FILE_UPLOAD_EXTENSION_NOT_ALLOWED);
        }
        return "." + extension;
    }
}
