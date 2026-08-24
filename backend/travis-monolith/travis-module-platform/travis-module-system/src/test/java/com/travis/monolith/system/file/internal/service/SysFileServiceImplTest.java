package com.travis.monolith.system.file.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travis.infrastructure.framework.event.core.TransactionalApplicationEventPublisher;
import com.travis.monolith.system.file.api.SysFileReferenceChecker;
import com.travis.monolith.system.file.api.SysFileUploaderNameResolver;
import com.travis.monolith.system.file.api.request.SysFilePageReq;
import com.travis.monolith.system.file.api.response.SysFileResp;
import com.travis.monolith.system.file.api.response.SysFileStorageConfigResp;
import com.travis.monolith.system.file.internal.converter.SysFileConverter;
import com.travis.monolith.system.file.internal.entity.SysFile;
import com.travis.monolith.system.file.internal.entity.SysFileStorageConfig;
import com.travis.monolith.system.file.internal.mapper.SysFileFolderMapper;
import com.travis.monolith.system.file.internal.mapper.SysFileMapper;
import com.travis.monolith.system.file.internal.mapper.SysFileStorageConfigMapper;
import com.travis.monolith.system.file.internal.service.impl.SysFileMetadataServiceImpl;
import com.travis.monolith.system.file.internal.service.impl.SysFileServiceImpl;
import com.travis.monolith.system.file.internal.strategy.FileStorageStrategy;
import com.travis.monolith.system.file.internal.strategy.StorageResult;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

class SysFileServiceImplTest {

    @Test
    void shouldRejectDeletingReferencedFile() {
        var mapper = mock(SysFileMapper.class);
        var checker = mock(SysFileReferenceChecker.class);
        when(checker.isReferenced(10L)).thenReturn(true);
        var service =
                service(
                        mapper,
                        List.of(),
                        provider(checker),
                        mock(SysFileStorageConfigService.class));
        var file = new SysFile();
        file.setId(10L);
        when(mapper.selectById(10L)).thenReturn(file);

        assertThatThrownBy(() -> service.deleteById(10L)).hasMessageContaining("文件仍被业务数据引用");

        verify(mapper, never()).deleteById(10L);
    }

    @Test
    void shouldDeleteStoredObjectWhenMetadataInsertFails() {
        var mapper = mock(SysFileMapper.class);
        var strategy = mock(FileStorageStrategy.class);
        var configService = mock(SysFileStorageConfigService.class);
        var config = new SysFileStorageConfig();
        config.setId(1L);
        config.setStorageType("LOCAL");
        when(configService.getDefaultInternalOrThrow()).thenReturn(config);
        when(strategy.getStorageType()).thenReturn("LOCAL");
        when(strategy.upload(any(), any())).thenReturn(new StorageResult("/files/a.png", "a.png"));
        when(mapper.insert(any(SysFile.class)))
                .thenThrow(new IllegalStateException("insert failed"));
        var service = service(mapper, List.of(strategy), provider(), configService);
        var file = new MockMultipartFile("file", "a.png", "image/png", new byte[] {1});

        assertThatThrownBy(() -> service.upload(file, 0L, "admin", 2L))
                .isInstanceOf(IllegalStateException.class);

        verify(strategy).delete("/files/a.png", config);
    }

    @Test
    void shouldReturnCurrentUploaderNameInFilePage() {
        var mapper = mock(SysFileMapper.class);
        var configService = mock(SysFileStorageConfigService.class);
        var converter = mock(SysFileConverter.class);
        var resolver = mock(SysFileUploaderNameResolver.class);
        var file = new SysFile();
        file.setId(10L);
        file.setUploaderType("admin");
        file.setUploaderId(20L);
        file.setStorageConfigId(1L);
        var response = new SysFileResp();
        response.setId(10L);
        response.setUploaderType("admin");
        response.setUploaderId(20L);
        var page = new Page<SysFile>(1, 10);
        page.setRecords(List.of(file));
        page.setTotal(1);
        var config = new SysFileStorageConfigResp();
        config.setId(1L);
        when(mapper.page(any(Integer.class), any(Integer.class), any())).thenReturn(page);
        when(configService.listAll()).thenReturn(List.of(config));
        when(converter.toResp(file)).thenReturn(response);
        when(resolver.getUploaderType()).thenReturn("admin");
        when(resolver.resolveNames(List.of(20L))).thenReturn(Map.of(20L, "travis"));
        @SuppressWarnings("unchecked")
        ObjectProvider<SysFileUploaderNameResolver> resolvers = mock(ObjectProvider.class);
        when(resolvers.orderedStream()).thenReturn(Stream.of(resolver));
        var service =
                new SysFileServiceImpl(
                        List.of(),
                        configService,
                        metadataService(mapper),
                        converter,
                        provider(),
                        resolvers,
                        mock(TransactionalApplicationEventPublisher.class));
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        var request = new SysFilePageReq();
        request.setPageNum(1);
        request.setPageSize(10);

        var result = service.page(request);

        assertThat(result.getRecords().getFirst().getUploaderName()).isEqualTo("travis");
    }

    private SysFileServiceImpl service(
            SysFileMapper mapper,
            List<FileStorageStrategy> strategies,
            ObjectProvider<SysFileReferenceChecker> checkers,
            SysFileStorageConfigService configService) {
        var service =
                new SysFileServiceImpl(
                        strategies,
                        configService,
                        metadataService(mapper),
                        mock(SysFileConverter.class),
                        checkers,
                        emptyUploaderNameResolvers(),
                        mock(TransactionalApplicationEventPublisher.class));
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        return service;
    }

    private SysFileMetadataService metadataService(SysFileMapper mapper) {
        var storageConfigMapper = mock(SysFileStorageConfigMapper.class);
        var config = new SysFileStorageConfig();
        config.setStorageType("LOCAL");
        when(storageConfigMapper.selectById(any())).thenReturn(config);
        return new SysFileMetadataServiceImpl(
                mapper, mock(SysFileFolderMapper.class), storageConfigMapper);
    }

    @SafeVarargs
    private final ObjectProvider<SysFileReferenceChecker> provider(
            SysFileReferenceChecker... checkers) {
        @SuppressWarnings("unchecked")
        ObjectProvider<SysFileReferenceChecker> provider = mock(ObjectProvider.class);
        when(provider.orderedStream()).thenReturn(Stream.of(checkers));
        return provider;
    }

    private ObjectProvider<SysFileUploaderNameResolver> emptyUploaderNameResolvers() {
        @SuppressWarnings("unchecked")
        ObjectProvider<SysFileUploaderNameResolver> provider = mock(ObjectProvider.class);
        when(provider.orderedStream()).thenReturn(Stream.empty());
        return provider;
    }
}
