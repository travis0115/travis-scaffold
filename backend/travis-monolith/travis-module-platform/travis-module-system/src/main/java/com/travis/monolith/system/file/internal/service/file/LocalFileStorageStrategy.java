package com.travis.monolith.system.file.internal.service.file;

import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.monolith.system.file.internal.entity.SysFileStorageConfig;
import com.travis.monolith.system.file.internal.service.FileStorageStrategy;
import com.travis.monolith.system.file.internal.service.StorageResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
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
public class LocalFileStorageStrategy implements FileStorageStrategy {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Environment environment;

    public LocalFileStorageStrategy(Environment environment) {
        this.environment = environment;
    }

    @Override
    public String getStorageType() {
        return "LOCAL";
    }

    @Override
    public StorageResult upload(MultipartFile file, SysFileStorageConfig config) {
        if (file == null || file.isEmpty()) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }

        // 获取原始文件名和扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 按日期分目录，UUID生成文件名
        String datePath = LocalDate.now().format(DATE_FORMAT);
        String filename = UUID.randomUUID().toString().replace("-", "") + extension;

        // 创建目标目录
        Path dirPath = Paths.get(environment.resolvePlaceholders(config.getBasePath()), datePath);
        try {
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            log.error("创建上传目录失败: {}", dirPath, e);
            throw new BizException(CommonErrorCode.FILE_UPLOAD_FAILED);
        }

        // 保存文件
        Path filePath = dirPath.resolve(filename);
        try {
            file.transferTo(filePath.toFile());
        } catch (IOException e) {
            log.error("文件保存失败: {}", filePath, e);
            throw new BizException(CommonErrorCode.FILE_UPLOAD_FAILED);
        }

        // 返回相对路径（去除resourceHandler中的/**通配符），调用方按需拼接域名
        String accessPrefix = config.getAccessPrefix().replaceAll("/+$", "");
        return new StorageResult(accessPrefix + "/" + datePath + "/" + filename, filename);
    }
}
