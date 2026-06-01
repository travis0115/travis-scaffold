package com.travis.monolith.system.internal.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 文件上传静态资源映射配置
 *
 * @author travis
 */
@Configuration
public class FileUploadWebMvcConfig implements WebMvcConfigurer {

    @Value("${travis.file.upload-path:./uploads}")
    private String uploadPath;

    @Value("${travis.file.url-prefix:/api/admin/system/file}")
    private String urlPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 urlPrefix/** 映射到本地上传目录
        registry.addResourceHandler(urlPrefix + "/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}
