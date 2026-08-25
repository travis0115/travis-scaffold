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
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
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
public class LocalFileResourceResolver extends AbstractResourceResolver {

    private static final long CONFIG_SNAPSHOT_TTL_NANOS = TimeUnit.SECONDS.toNanos(5);

    private final SysFileStorageConfigService storageConfigService;
    private final Environment environment;
    private final LongSupplier nanoTime;
    private volatile StorageConfigSnapshot storageConfigSnapshot = StorageConfigSnapshot.empty();

    public LocalFileResourceResolver(
            SysFileStorageConfigService storageConfigService, Environment environment) {
        this(storageConfigService, environment, System::nanoTime);
    }

    LocalFileResourceResolver(
            SysFileStorageConfigService storageConfigService,
            Environment environment,
            LongSupplier nanoTime) {
        this.storageConfigService = storageConfigService;
        this.environment = environment;
        this.nanoTime = nanoTime;
    }

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
        for (var config : currentStorageConfigs()) {
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

    private List<SysFileStorageConfig> currentStorageConfigs() {
        var now = nanoTime.getAsLong();
        var current = storageConfigSnapshot;
        if (current.isFresh(now)) {
            return current.configs();
        }
        synchronized (this) {
            current = storageConfigSnapshot;
            now = nanoTime.getAsLong();
            if (current.isFresh(now)) {
                return current.configs();
            }
            try {
                var configs = List.copyOf(storageConfigService.listEnabledLocalConfigs());
                storageConfigSnapshot = new StorageConfigSnapshot(configs, now, true);
                return configs;
            } catch (RuntimeException exception) {
                if (!current.loaded()) {
                    throw exception;
                }
                storageConfigSnapshot = new StorageConfigSnapshot(current.configs(), now, true);
                logger.warn("刷新本地文件存储配置失败，继续使用上一份快照", exception);
                return current.configs();
            }
        }
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

    private record StorageConfigSnapshot(
            List<SysFileStorageConfig> configs, long loadedAtNanos, boolean loaded) {

        private static StorageConfigSnapshot empty() {
            return new StorageConfigSnapshot(List.of(), 0, false);
        }

        private boolean isFresh(long now) {
            return loaded && now - loadedAtNanos < CONFIG_SNAPSHOT_TTL_NANOS;
        }
    }
}
