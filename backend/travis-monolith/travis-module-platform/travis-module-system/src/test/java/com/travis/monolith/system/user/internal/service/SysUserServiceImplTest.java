package com.travis.monolith.system.user.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.dev33.satoken.stp.StpLogic;
import com.baomidou.mybatisplus.annotation.Version;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travis.infrastructure.common.validation.validator.PasswordValidator;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.framework.event.core.TransactionalApplicationEventPublisher;
import com.travis.infrastructure.framework.satoken.core.StpKit;
import com.travis.monolith.system.dept.api.SysDeptApi;
import com.travis.monolith.system.file.api.SysFileApi;
import com.travis.monolith.system.role.api.SysRoleApi;
import com.travis.monolith.system.user.api.request.SysUserCreateReq;
import com.travis.monolith.system.user.api.request.SysUserPageReq;
import com.travis.monolith.system.user.api.request.SysUserRoleReq;
import com.travis.monolith.system.user.api.response.SysUserResp;
import com.travis.monolith.system.user.internal.converter.SysUserConverter;
import com.travis.monolith.system.user.internal.entity.SysUser;
import com.travis.monolith.system.user.internal.mapper.SysUserMapper;
import com.travis.monolith.system.user.internal.model.SysUserCountSummary;
import com.travis.monolith.system.user.internal.service.impl.SysUserServiceImpl;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;

class SysUserServiceImplTest {

    @Test
    void shouldEnableOptimisticLockOnUserVersion() throws NoSuchFieldException {
        assertThat(SysUser.class.getDeclaredField("lockVersion").isAnnotationPresent(Version.class))
                .isTrue();
    }

    @Test
    void shouldRejectCreatingUserWithMissingDept() {
        var mapper = mock(SysUserMapper.class);
        var deptApi = mock(SysDeptApi.class);
        when(deptApi.existsAnyByIds(List.of(99L))).thenReturn(false);
        var service = service(mapper, deptApi);
        var request = new SysUserCreateReq();
        request.setDeptId(99L);

        assertThatThrownBy(() -> service.create(request)).hasMessageContaining("所属部门不存在");
    }

    @Test
    void shouldReportOptimisticLockConflict() {
        var mapper = mock(SysUserMapper.class);
        var user = new SysUser();
        user.setId(1L);
        user.setLockVersion(2);
        when(mapper.selectById(1L)).thenReturn(user);
        when(mapper.updateById(user)).thenReturn(0);
        var service = service(mapper, mock(SysDeptApi.class));

        assertThatThrownBy(() -> service.updateStatus(1L, 0)).hasMessageContaining("已被其他请求修改");
    }

    @ParameterizedTest
    @CsvSource({"2, 3, 研发部, /files/avatar.png", "0, , ,", ", , ,"})
    void shouldBatchLoadPageAssociations(
            Long deptId, Long avatarFileId, String deptName, String avatarUrl) {
        var mapper = mock(SysUserMapper.class);
        var deptApi = mock(SysDeptApi.class);
        var roleApi = mock(SysRoleApi.class);
        var fileApi = mock(SysFileApi.class);
        var converter = mock(SysUserConverter.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<
                        com.travis.infrastructure.framework.websocket.core.session
                                .WebSocketSessionManager>
                sessionManagers = mock(ObjectProvider.class);
        var service =
                new SysUserServiceImpl(
                        deptApi,
                        roleApi,
                        fileApi,
                        mock(TransactionalApplicationEventPublisher.class),
                        sessionManagers,
                        converter);
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        var user = new SysUser();
        user.setId(1L);
        user.setDeptId(deptId);
        user.setAvatarFileId(avatarFileId);
        var response = new SysUserResp();
        response.setId(1L);
        var page = new Page<SysUser>(1, 10);
        page.setRecords(List.of(user));
        page.setTotal(1);
        when(mapper.page(any(Integer.class), any(Integer.class), any())).thenReturn(page);
        when(converter.toResp(user)).thenReturn(response);
        when(deptApi.getDeptNameMapByIds(any()))
                .thenReturn(deptName == null ? Map.of() : Map.of(deptId, deptName));
        when(roleApi.getRoleNameMapByUserIds(List.of(1L))).thenReturn(Map.of(1L, List.of("管理员")));
        when(fileApi.getFileUrlMapByIds(any()))
                .thenReturn(avatarUrl == null ? Map.of() : Map.of(avatarFileId, avatarUrl));
        var request = new SysUserPageReq();
        request.setPageNum(1);
        request.setPageSize(10);

        var result = service.page(request);

        assertThat(result.getRecords().getFirst().getDeptName()).isEqualTo(deptName);
        assertThat(result.getRecords().getFirst().getRoleNames()).containsExactly("管理员");
        assertThat(result.getRecords().getFirst().getAvatar()).isEqualTo(avatarUrl);
        verify(deptApi, never()).getDeptNameById(any());
        verify(roleApi, never()).getRoleNamesByUserId(any());
        verify(fileApi, never()).getFileUrlById(any());
    }

    @Test
    void shouldReturnUserDashboardSummary() {
        var mapper = mock(SysUserMapper.class);
        when(mapper.selectCountSummary(any())).thenReturn(new SysUserCountSummary(12, 2));
        var service = service(mapper, mock(SysDeptApi.class));

        var result = service.dashboard();

        assertThat(result.totalUsers()).isEqualTo(12);
        assertThat(result.newUsersToday()).isEqualTo(2);
        assertThat(result.onlineUsers()).isZero();
    }

    @Test
    void shouldGenerateCompliantPasswordWhenResetPasswordIsEmpty() {
        var mapper = mock(SysUserMapper.class);
        var user = new SysUser();
        user.setId(1L);
        when(mapper.selectById(1L)).thenReturn(user);
        when(mapper.updateById(user)).thenReturn(1);
        var service = service(mapper, mock(SysDeptApi.class));
        var logic = mock(StpLogic.class);

        String password;
        try (var stpKit = mockStatic(StpKit.class)) {
            stpKit.when(() -> StpKit.of(LoginType.ADMIN)).thenReturn(logic);
            password = service.resetPassword(1L, null);
        }

        assertThat(password).hasSize(12);
        assertThat(new PasswordValidator().isValid(password, null)).isTrue();
    }

    @Test
    void shouldRejectNonSuperAdminOperatingSuperAdmin() {
        var mapper = mock(SysUserMapper.class);
        var roleApi = mock(SysRoleApi.class);
        var user = new SysUser();
        user.setId(1L);
        when(mapper.selectById(1L)).thenReturn(user);
        when(roleApi.getRoleCodesByUserId(1L)).thenReturn(List.of("admin"));
        var service = service(mapper, mock(SysDeptApi.class), roleApi);
        var logic = mock(StpLogic.class);
        when(logic.hasRole("admin")).thenReturn(false);

        try (var stpKit = mockStatic(StpKit.class)) {
            stpKit.when(() -> StpKit.of(LoginType.ADMIN)).thenReturn(logic);

            assertThatThrownBy(() -> service.resetPassword(1L, "NewPassword1"))
                    .hasMessageContaining("仅超级管理员");
        }
        verify(mapper, never()).updateById(any(SysUser.class));
    }

    @Test
    void shouldRejectNonSuperAdminGrantingSuperAdminRole() {
        var mapper = mock(SysUserMapper.class);
        var roleApi = mock(SysRoleApi.class);
        var user = new SysUser();
        user.setId(2L);
        when(mapper.selectById(2L)).thenReturn(user);
        when(roleApi.getRoleCodesByUserId(2L)).thenReturn(List.of());
        when(roleApi.getRoleCodesByRoleIds(List.of(1L))).thenReturn(List.of("admin"));
        var service = service(mapper, mock(SysDeptApi.class), roleApi);
        var logic = mock(StpLogic.class);
        when(logic.hasRole("admin")).thenReturn(false);
        var request = new SysUserRoleReq();
        request.setUserId(2L);
        request.setRoleIds(List.of(1L));

        try (var stpKit = mockStatic(StpKit.class)) {
            stpKit.when(() -> StpKit.of(LoginType.ADMIN)).thenReturn(logic);

            assertThatThrownBy(() -> service.assignRoles(request)).hasMessageContaining("仅超级管理员");
        }
        verify(roleApi, never()).assignUserRoles(any(), any());
    }

    @Test
    void shouldRejectRemovingRoleFromLastEnabledSuperAdmin() {
        var mapper = mock(SysUserMapper.class);
        var roleApi = mock(SysRoleApi.class);
        var user = new SysUser();
        user.setId(1L);
        user.setStatus(1);
        when(mapper.selectById(1L)).thenReturn(user);
        when(mapper.selectCount(any())).thenReturn(0L);
        when(roleApi.getRoleCodesByUserId(1L)).thenReturn(List.of("admin"));
        when(roleApi.getRoleCodesByRoleIds(List.of())).thenReturn(List.of());
        when(roleApi.getUserIdsByRoleCode("admin")).thenReturn(List.of(1L));
        var service = service(mapper, mock(SysDeptApi.class), roleApi);
        var logic = mock(StpLogic.class);
        when(logic.hasRole("admin")).thenReturn(true);
        var request = new SysUserRoleReq();
        request.setUserId(1L);
        request.setRoleIds(List.of());

        try (var stpKit = mockStatic(StpKit.class)) {
            stpKit.when(() -> StpKit.of(LoginType.ADMIN)).thenReturn(logic);

            assertThatThrownBy(() -> service.assignRoles(request))
                    .hasMessageContaining("至少一个启用的超级管理员");
        }
        verify(roleApi, never()).assignUserRoles(any(), any());
    }

    private SysUserServiceImpl service(SysUserMapper mapper, SysDeptApi deptApi) {
        return service(mapper, deptApi, mock(SysRoleApi.class));
    }

    private SysUserServiceImpl service(
            SysUserMapper mapper, SysDeptApi deptApi, SysRoleApi roleApi) {
        @SuppressWarnings("unchecked")
        ObjectProvider<
                        com.travis.infrastructure.framework.websocket.core.session
                                .WebSocketSessionManager>
                sessionManagers = mock(ObjectProvider.class);
        var service =
                new SysUserServiceImpl(
                        deptApi,
                        roleApi,
                        mock(SysFileApi.class),
                        mock(TransactionalApplicationEventPublisher.class),
                        sessionManagers,
                        mock(SysUserConverter.class));
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        return service;
    }
}
