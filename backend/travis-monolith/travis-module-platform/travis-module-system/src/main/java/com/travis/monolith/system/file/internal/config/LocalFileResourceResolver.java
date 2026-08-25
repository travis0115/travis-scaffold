package com.travis.monolith.system.file.internal.config;

import com.travis.monolith.system.file.internal.entity.SysFileStorageConfig;
import com.travis.monolith.system.file.internal.service.SysFileStorageConfigService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.resource.AbstractResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

/** 根据当前启用的本地存储配置动态解析文件资源。 */
@Component
@RequiredArgsConstructor
public class LocalFileResourceResolver extends AbstractResourceResolver {

    private final SysFileStorageConfigService storageConfigService;
    private final Environment environment;

    @Override
    protected @Nullable Resource resolveResourceInternal(
            @Nullable HttpServletRequest request,
            String requestPath,
            List<? extends Resource> locations,
            ResourceResolverChain chain) {
        if (!StringUtils.hasText(requestPath)) {
            return null;
        }
        var relativePath = requestPath.replaceFirst("^/+", "");
        for (var config : storageConfigService.listEnabledLocalConfigs()) {
            var resource = resolve(config, relativePath);
            if (resource != null) {
                return resource;
            }
        }
        return null;
    }

    @Override
    protected @Nullable String resolveUrlPathInternal(
            String resourceUrlPath,
            List<? extends Resource> locations,
            ResourceResolverChain chain) {
        return resolveResourceInternal(null, resourceUrlPath, locations, chain) == null
                ? null
                : resourceUrlPath;
    }

    private @Nullable Resource resolve(SysFileStorageConfig config, String relativePath) {
        if (!StringUtils.hasText(config.getStoragePath())) {
            return null;
        }
        try {
            Path root =
                    Paths.get(environment.resolvePlaceholders(config.getStoragePath()))
                            .toAbsolutePath()
                            .normalize();
            Path candidate = root.resolve(relativePath).normalize();
            if (!candidate.startsWith(root)
                    || !Files.isRegularFile(candidate)
                    || !Files.isReadable(candidate)) {
                return null;
            }
            Path realRoot = root.toRealPath();
            Path realCandidate = candidate.toRealPath();
            return realCandidate.startsWith(realRoot)
                    ? new FileSystemResource(realCandidate)
                    : null;
        } catch (IOException | InvalidPathException exception) {
            logger.debug("无法解析本地文件资源: " + relativePath, exception);
            return null;
        }
    }
}
