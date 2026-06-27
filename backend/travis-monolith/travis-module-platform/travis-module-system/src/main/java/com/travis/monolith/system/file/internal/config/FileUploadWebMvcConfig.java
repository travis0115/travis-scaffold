package com.travis.monolith.system.file.internal.config;

import com.travis.monolith.system.file.internal.config.properties.FileUploadProperties;
import com.travis.monolith.system.file.internal.entity.SysFileStorageConfig;
import com.travis.monolith.system.file.internal.service.SysFileStorageConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 文件上传静态资源映射配置 开发环境：由 Spring Boot 直接提供静态资源访问 生产环境：建议由 Nginx 提供 /files/ 的静态资源服务，性能更优
 *
 * @author travis
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class FileUploadWebMvcConfig implements WebMvcConfigurer {

    private static final String RESOURCE_HANDLER = "/files/**";

    private final SysFileStorageConfigService storageConfigService;
    private final Environment environment;
    private final FileUploadProperties fileUploadProperties;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        var locations =
                storageConfigService.listEnabledLocalConfigs().stream()
                        .map(SysFileStorageConfig::getStoragePath)
                        .filter(StringUtils::hasText)
                        .map(environment::resolvePlaceholders)
                        .map(this::toResourceLocation)
                        .distinct()
                        .toArray(String[]::new);
        var resourceHandler = fileUploadProperties.getResourceHandler();
        if (!StringUtils.hasText(resourceHandler)) {
            resourceHandler = RESOURCE_HANDLER;
        }
        if (locations.length == 0) {
            log.warn("未找到启用的本地存储配置，跳过 {} 静态资源映射", resourceHandler);
            return;
        }
        registry.addResourceHandler(resourceHandler).addResourceLocations(locations);
    }

    private String toResourceLocation(String storagePath) {
        var location = storagePath.endsWith("/") ? storagePath : storagePath + "/";
        return "file:" + location;
    }
}
