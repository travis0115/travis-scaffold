package com.travis.monolith.system.dept.internal.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.travis.infrastructure.framework.event.core.TransactionalApplicationEventPublisher;
import com.travis.monolith.system.dept.api.request.SysDeptCreateReq;
import com.travis.monolith.system.dept.internal.converter.SysDeptConverter;
import com.travis.monolith.system.dept.internal.entity.SysDept;
import com.travis.monolith.system.dept.internal.mapper.SysDeptMapper;
import com.travis.monolith.system.dept.internal.service.impl.SysDeptServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class SysDeptServiceImplTest {

    @Test
    void shouldRejectMissingParentWhenCreatingDepartment() {
        var mapper = mock(SysDeptMapper.class);
        var converter = mock(SysDeptConverter.class);
        var service =
                new SysDeptServiceImpl(
                        converter, mock(TransactionalApplicationEventPublisher.class));
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        var request = new SysDeptCreateReq();
        request.setParentId(99L);

        assertThatThrownBy(() -> service.create(request)).hasMessageContaining("上级部门不存在");

        verify(converter, never()).toEntity(request);
        verify(mapper, never()).insert(org.mockito.ArgumentMatchers.any(SysDept.class));
    }
}
