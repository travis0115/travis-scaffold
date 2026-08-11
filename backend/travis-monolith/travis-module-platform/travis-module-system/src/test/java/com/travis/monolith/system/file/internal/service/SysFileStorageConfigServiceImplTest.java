package com.travis.monolith.system.file.internal.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.travis.monolith.system.file.api.request.SysFileStorageConfigUpdateReq;
import com.travis.monolith.system.file.internal.converter.SysFileStorageConfigConverter;
import com.travis.monolith.system.file.internal.entity.SysFileStorageConfig;
import com.travis.monolith.system.file.internal.mapper.SysFileStorageConfigMapper;
import com.travis.monolith.system.file.internal.service.impl.SysFileStorageConfigServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class SysFileStorageConfigServiceImplTest {

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
}
