package com.travis.monolith.system.file.internal.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.travis.monolith.system.common.api.BuiltinResourceGuard;
import com.travis.monolith.system.file.api.request.SysFileFolderCreateReq;
import com.travis.monolith.system.file.internal.converter.SysFileFolderConverter;
import com.travis.monolith.system.file.internal.mapper.SysFileFolderMapper;
import com.travis.monolith.system.file.internal.service.impl.SysFileFolderServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class SysFileFolderServiceImplTest {

    @Test
    void shouldRejectMissingParentFolder() {
        var mapper = mock(SysFileFolderMapper.class);
        var service =
                new SysFileFolderServiceImpl(
                        mock(SysFileFolderConverter.class),
                        mock(SysFileService.class),
                        mock(BuiltinResourceGuard.class));
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        var request = new SysFileFolderCreateReq();
        request.setParentId(99L);

        assertThatThrownBy(() -> service.create(request)).hasMessageContaining("上级文件夹不存在");
    }
}
