package com.travis.monolith.system.user.internal.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.dev33.satoken.stp.StpLogic;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.framework.satoken.core.StpKit;
import com.travis.monolith.system.dept.api.SysDeptApi;
import com.travis.monolith.system.user.internal.entity.SysUser;
import com.travis.monolith.system.user.internal.service.SysUserService;
import java.util.List;
import org.junit.jupiter.api.Test;

class SysUserApiImplTest {

    @Test
    void shouldNotRestrictSuperAdminUserOptionsByDepartment() {
        var userService = mock(SysUserService.class);
        var deptApi = mock(SysDeptApi.class);
        var api = new SysUserApiImpl(userService, deptApi);
        var logic = mock(StpLogic.class);
        when(logic.hasRole("admin")).thenReturn(true);
        when(userService.list(org.mockito.ArgumentMatchers.<Wrapper<SysUser>>any()))
                .thenReturn(List.of());

        try (var stpKit = mockStatic(StpKit.class)) {
            stpKit.when(() -> StpKit.of(LoginType.ADMIN)).thenReturn(logic);
            api.listCurrentUserScopedOptions(null, 20);
        }

        verify(logic, never()).getLoginIdAsLong();
        verify(userService, never()).getDetailByIdOrThrow(any());
        verify(deptApi, never()).listSelfAndDescendantIds(any());
    }
}
