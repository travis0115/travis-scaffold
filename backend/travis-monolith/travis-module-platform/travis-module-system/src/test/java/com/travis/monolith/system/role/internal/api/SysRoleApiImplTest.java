package com.travis.monolith.system.role.internal.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.travis.monolith.system.role.internal.service.SysRoleService;
import java.util.List;
import org.junit.jupiter.api.Test;

class SysRoleApiImplTest {

    @Test
    void shouldOnlyUseEnabledRolesForAuthorizationCodes() {
        var roleService = mock(SysRoleService.class);
        when(roleService.getEnabledRoleIdsByUserId(10L)).thenReturn(List.of(2L));
        when(roleService.getRoleCodeByRoleId(2L)).thenReturn("operator");
        var roleApi = new SysRoleApiImpl(roleService);

        assertThat(roleApi.getRoleCodesByUserId(10L)).containsExactly("operator");

        verify(roleService).getEnabledRoleIdsByUserId(10L);
    }

    @Test
    void shouldKeepAllAssignedRolesForManagementDisplay() {
        var roleService = mock(SysRoleService.class);
        when(roleService.getRoleIdsByUserId(10L)).thenReturn(List.of(1L, 2L));
        when(roleService.getRoleNamesByRoleIds(List.of(1L, 2L)))
                .thenReturn(List.of("已禁用角色", "启用角色"));
        var roleApi = new SysRoleApiImpl(roleService);

        assertThat(roleApi.getRoleNamesByUserId(10L)).containsExactly("已禁用角色", "启用角色");

        verify(roleService).getRoleIdsByUserId(10L);
    }
}
