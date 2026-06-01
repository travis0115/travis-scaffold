package com.travis.monolith.system.internal.service.file;

import com.travis.infrastructure.framework.web.core.exception.BizException;
import com.travis.infrastructure.framework.web.core.exception.CommonErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 本地文件存储策略
 * 将文件保存到服务器本地磁盘，通过静态资源映射提供访问
 *
 * @author travis
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "travis.file.storage-type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageStrategy implements FileStorageStrategy {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @Value("${travis.file.upload-path:./uploads}")
    private String uploadPath;

    @Value("${travis.file.url-prefix:/api/admin/system/file}")
    private String urlPrefix;

    @Override
    public String upload(MultipartFile file) {
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
        Path dirPath = Paths.get(uploadPath, datePath);
        try {
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            log.error("创建上传目录失败: {}", dirPath, e);
            throw new BizException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }

        // 保存文件
        Path filePath = dirPath.resolve(filename);
        try {
            file.transferTo(filePath.toFile());
        } catch (IOException e) {
            log.error("文件保存失败: {}", filePath, e);
            throw new BizException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }

        // 返回访问URL
        return urlPrefix + "/" + datePath + "/" + filename;
    }
}
