package com.travis.monolith.system.file.internal.config;

import com.travis.monolith.system.file.internal.config.properties.FileUploadProperties;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** 文件上传静态资源映射配置，根据当前启用的本地存储配置动态解析资源。 */
@Configuration
@RequiredArgsConstructor
public class FileUploadWebMvcConfig implements WebMvcConfigurer {

    private static final String RESOURCE_HANDLER = "/files/**";

    private final FileUploadProperties fileUploadProperties;
    private final LocalFileResourceResolver localFileResourceResolver;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        var resourceHandler = fileUploadProperties.getResourceHandler();
        if (!StringUtils.hasText(resourceHandler)) {
            resourceHandler = RESOURCE_HANDLER;
        }
        registry.addResourceHandler(resourceHandler)
                .resourceChain(false)
                .addResolver(localFileResourceResolver);
    }
}
