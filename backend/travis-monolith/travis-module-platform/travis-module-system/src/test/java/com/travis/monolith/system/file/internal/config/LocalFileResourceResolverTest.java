package com.travis.monolith.system.file.internal.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.travis.monolith.system.file.internal.entity.SysFileStorageConfig;
import com.travis.monolith.system.file.internal.service.SysFileStorageConfigService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.servlet.resource.ResourceResolverChain;

class LocalFileResourceResolverTest {

    @TempDir Path tempDir;

    @Test
    void shouldCacheStorageConfigsAndRefreshExpiredSnapshot() throws Exception {
        var firstRoot = Files.createDirectory(tempDir.resolve("first"));
        var secondRoot = Files.createDirectory(tempDir.resolve("second"));
        Files.writeString(firstRoot.resolve("first.txt"), "first");
        Files.writeString(secondRoot.resolve("second.txt"), "second");
        var nanoTime = new AtomicLong();
        var service = mock(SysFileStorageConfigService.class);
        when(service.listEnabledLocalConfigs())
                .thenReturn(List.of(config(firstRoot)))
                .thenReturn(List.of(config(secondRoot)));
        var resolver = new LocalFileResourceResolver(service, new MockEnvironment(), nanoTime::get);
        var chain = mock(ResourceResolverChain.class);

        var first = resolver.resolveResource(null, "first.txt", List.of(), chain);
        var cached = resolver.resolveResource(null, "first.txt", List.of(), chain);
        nanoTime.addAndGet(Duration.ofSeconds(6).toNanos());
        var second = resolver.resolveResource(null, "second.txt", List.of(), chain);

        assertThat(first).isInstanceOf(FileSystemResource.class);
        assertThat(cached).isInstanceOf(FileSystemResource.class);
        assertThat(((FileSystemResource) first).getPath())
                .isEqualTo(firstRoot.resolve("first.txt").toRealPath().toString());
        assertThat(second).isInstanceOf(FileSystemResource.class);
        assertThat(((FileSystemResource) second).getPath())
                .isEqualTo(secondRoot.resolve("second.txt").toRealPath().toString());
        verify(service, times(2)).listEnabledLocalConfigs();
    }

    @Test
    void shouldUsePreviousSnapshotWhenRefreshFails() throws Exception {
        var storageRoot = Files.createDirectory(tempDir.resolve("storage"));
        Files.writeString(storageRoot.resolve("file.txt"), "content");
        var nanoTime = new AtomicLong();
        var service = mock(SysFileStorageConfigService.class);
        when(service.listEnabledLocalConfigs())
                .thenReturn(List.of(config(storageRoot)))
                .thenThrow(new IllegalStateException("redis down"));
        var resolver = new LocalFileResourceResolver(service, new MockEnvironment(), nanoTime::get);

        var first =
                resolver.resolveResource(
                        null, "file.txt", List.of(), mock(ResourceResolverChain.class));
        nanoTime.addAndGet(Duration.ofSeconds(6).toNanos());
        var stale =
                resolver.resolveResource(
                        null, "file.txt", List.of(), mock(ResourceResolverChain.class));

        assertThat(first).isInstanceOf(FileSystemResource.class);
        assertThat(stale).isInstanceOf(FileSystemResource.class);
        verify(service, times(2)).listEnabledLocalConfigs();
    }

    @Test
    void shouldRejectPathsOutsideStorageRoot() throws Exception {
        var storageRoot = Files.createDirectory(tempDir.resolve("storage"));
        Files.writeString(tempDir.resolve("outside.txt"), "outside");
        var service = mock(SysFileStorageConfigService.class);
        when(service.listEnabledLocalConfigs()).thenReturn(List.of(config(storageRoot)));
        var resolver = new LocalFileResourceResolver(service, new MockEnvironment());

        var resource =
                resolver.resolveResource(
                        null, "../outside.txt", List.of(), mock(ResourceResolverChain.class));

        assertThat(resource).isNull();
    }

    @Test
    void shouldRejectSymbolicLinksOutsideStorageRoot() throws Exception {
        var storageRoot = Files.createDirectory(tempDir.resolve("symlink-storage"));
        var outside = Files.writeString(tempDir.resolve("symlink-outside.txt"), "outside");
        Files.createSymbolicLink(storageRoot.resolve("link.txt"), outside);
        var service = mock(SysFileStorageConfigService.class);
        when(service.listEnabledLocalConfigs()).thenReturn(List.of(config(storageRoot)));
        var resolver = new LocalFileResourceResolver(service, new MockEnvironment());

        var resource =
                resolver.resolveResource(
                        null, "link.txt", List.of(), mock(ResourceResolverChain.class));

        assertThat(resource).isNull();
    }

    @Test
    void shouldResolveStoragePathPlaceholders() throws Exception {
        var storageRoot = Files.createDirectory(tempDir.resolve("placeholder-storage"));
        Files.writeString(storageRoot.resolve("file.txt"), "content");
        var service = mock(SysFileStorageConfigService.class);
        when(service.listEnabledLocalConfigs())
                .thenReturn(List.of(config(Path.of("${review.storage-root}"))));
        var environment =
                new MockEnvironment().withProperty("review.storage-root", storageRoot.toString());
        var resolver = new LocalFileResourceResolver(service, environment);

        var resource =
                resolver.resolveResource(
                        null, "file.txt", List.of(), mock(ResourceResolverChain.class));

        assertThat(resource).isInstanceOf(FileSystemResource.class);
        assertThat(((FileSystemResource) resource).getPath())
                .isEqualTo(storageRoot.resolve("file.txt").toRealPath().toString());
    }

    private SysFileStorageConfig config(Path storagePath) {
        var config = new SysFileStorageConfig();
        config.setStoragePath(storagePath.toString());
        return config;
    }
}
