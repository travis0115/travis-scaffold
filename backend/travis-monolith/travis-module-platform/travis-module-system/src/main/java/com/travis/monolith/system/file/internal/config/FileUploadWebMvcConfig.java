package com.travis.monolith.system.file.internal.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 文件上传静态资源映射配置 开发环境：由 Spring Boot 直接提供静态资源访问 生产环境：建议由 Nginx 提供 /files/ 的静态资源服务，性能更优
 *
 * @author travis
 */
@Configuration
public class FileUploadWebMvcConfig implements WebMvcConfigurer {

    @Value("${travis.web.file.resource-location:./uploads}")
    private String resourceLocation;

    @Value("${travis.web.file.resource-handler:/files/**}")
    private String resourceHandler;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(resourceHandler)
                .addResourceLocations("file:" + resourceLocation);
    }
}
