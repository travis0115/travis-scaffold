package com.travis.monolith.system.menu.internal.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.travis.monolith.system.menu.api.enums.MenuType;
import com.travis.monolith.system.menu.api.request.SysMenuCreateReq;
import com.travis.monolith.system.menu.internal.converter.SysMenuConverter;
import com.travis.monolith.system.menu.internal.entity.SysMenu;
import com.travis.monolith.system.menu.internal.mapper.SysMenuMapper;
import com.travis.monolith.system.menu.internal.service.impl.SysMenuServiceImpl;
import com.travis.monolith.system.role.api.SysRoleApi;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class SysMenuServiceImplTest {

    @Test
    void shouldRejectButtonAsParentMenu() {
        var mapper = mock(SysMenuMapper.class);
        var parent = new SysMenu();
        parent.setId(9L);
        parent.setMenuType(MenuType.BUTTON.getValue());
        when(mapper.selectById(9L)).thenReturn(parent);
        var service = new SysMenuServiceImpl(mock(SysRoleApi.class), mock(SysMenuConverter.class));
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        var request = new SysMenuCreateReq();
        request.setParentId(9L);
        request.setMenuType(MenuType.MENU.getValue());

        assertThatThrownBy(() -> service.create(request)).hasMessageContaining("上级菜单不存在、类型不正确");
    }
}
