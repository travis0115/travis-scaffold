package com.travis.monolith.system.file.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.travis.monolith.system.file.api.request.SysFileStorageConfigUpdateReq;
import com.travis.monolith.system.file.api.response.SysFileStorageConfigResp;
import com.travis.monolith.system.file.internal.converter.SysFileStorageConfigConverter;
import com.travis.monolith.system.file.internal.entity.SysFileStorageConfig;
import com.travis.monolith.system.file.internal.mapper.SysFileStorageConfigMapper;
import com.travis.monolith.system.file.internal.service.impl.SysFileStorageConfigServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class SysFileStorageConfigServiceImplTest {

    @Test
    void responseShouldNotExposeSecretKeyField() {
        assertThat(SysFileStorageConfigResp.class.getDeclaredFields())
                .extracting("name")
                .contains("secretConfigured")
                .doesNotContain("secretKey");
    }

    @Test
    void shouldRejectChangingLocationOfStorageConfigInUse() {
        var mapper = mock(SysFileStorageConfigMapper.class);
        var service =
                new SysFileStorageConfigServiceImpl(mock(SysFileStorageConfigConverter.class));
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        var old = new SysFileStorageConfig();
        old.setId(1L);
        old.setStorageType("LOCAL");
        old.setStoragePath("/old");
        old.setIsDefault(0);
        when(mapper.selectById(1L)).thenReturn(old);
        when(mapper.existsFile(1L)).thenReturn(true);
        var request = new SysFileStorageConfigUpdateReq();
        request.setStorageType("LOCAL");
        request.setStoragePath("/new");
        request.setIsDefault(0);
        request.setStatus(1);

        assertThatThrownBy(() -> service.update(1L, request)).hasMessageContaining("不允许修改存储位置");
    }

    @Test
    void shouldKeepExistingCredentialsWhenSecretKeyIsBlank() {
        var mapper = mock(SysFileStorageConfigMapper.class);
        var converter = mock(SysFileStorageConfigConverter.class);
        var service = new SysFileStorageConfigServiceImpl(converter);
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        var old = new SysFileStorageConfig();
        old.setId(1L);
        old.setStorageType("S3");
        old.setStoragePath("/");
        old.setAccessKey("old-access-key");
        old.setSecretKey("old-secret-key");
        old.setIsDefault(0);
        when(mapper.selectById(1L)).thenReturn(old);
        when(mapper.existsFile(1L)).thenReturn(false);
        when(mapper.updateById(any(SysFileStorageConfig.class))).thenReturn(1);
        var request = new SysFileStorageConfigUpdateReq();
        request.setLockVersion(1);
        request.setStorageType("S3");
        request.setStoragePath("/");
        request.setAccessKey("ignored-access-key");
        request.setSecretKey(" ");
        request.setIsDefault(0);
        request.setStatus(1);
        when(converter.update(request, old))
                .thenAnswer(
                        invocation -> {
                            old.setAccessKey(request.getAccessKey());
                            old.setSecretKey(request.getSecretKey());
                            return old;
                        });

        service.update(1L, request);

        var captor = ArgumentCaptor.forClass(SysFileStorageConfig.class);
        verify(mapper).updateById(captor.capture());
        assertThat(captor.getValue().getAccessKey()).isEqualTo("old-access-key");
        assertThat(captor.getValue().getSecretKey()).isEqualTo("old-secret-key");
    }
}
