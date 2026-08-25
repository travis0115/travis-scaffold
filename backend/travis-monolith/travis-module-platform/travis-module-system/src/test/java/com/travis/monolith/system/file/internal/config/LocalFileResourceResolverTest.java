package com.travis.monolith.system.file.internal.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.travis.monolith.system.file.internal.entity.SysFileStorageConfig;
import com.travis.monolith.system.file.internal.service.SysFileStorageConfigService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.servlet.resource.ResourceResolverChain;

class LocalFileResourceResolverTest {

    @TempDir Path tempDir;

    @Test
    void shouldResolveFilesFromCurrentStorageConfigs() throws Exception {
        var firstRoot = Files.createDirectory(tempDir.resolve("first"));
        var secondRoot = Files.createDirectory(tempDir.resolve("second"));
        Files.writeString(firstRoot.resolve("first.txt"), "first");
        Files.writeString(secondRoot.resolve("second.txt"), "second");
        var service = mock(SysFileStorageConfigService.class);
        when(service.listEnabledLocalConfigs())
                .thenReturn(List.of(config(firstRoot)))
                .thenReturn(List.of(config(secondRoot)));
        var resolver = new LocalFileResourceResolver(service, new MockEnvironment());
        var chain = mock(ResourceResolverChain.class);

        var first = resolver.resolveResource(null, "first.txt", List.of(), chain);
        var second = resolver.resolveResource(null, "second.txt", List.of(), chain);

        assertThat(first).isInstanceOf(FileSystemResource.class);
        assertThat(((FileSystemResource) first).getPath())
                .isEqualTo(firstRoot.resolve("first.txt").toRealPath().toString());
        assertThat(second).isInstanceOf(FileSystemResource.class);
        assertThat(((FileSystemResource) second).getPath())
                .isEqualTo(secondRoot.resolve("second.txt").toRealPath().toString());
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

    private SysFileStorageConfig config(Path storagePath) {
        var config = new SysFileStorageConfig();
        config.setStoragePath(storagePath.toString());
        return config;
    }
}
