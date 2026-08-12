package com.travis.monolith.system.user.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.annotation.Version;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travis.infrastructure.framework.event.core.TransactionalApplicationEventPublisher;
import com.travis.monolith.system.dept.api.SysDeptApi;
import com.travis.monolith.system.file.api.SysFileApi;
import com.travis.monolith.system.role.api.SysRoleApi;
import com.travis.monolith.system.user.api.request.SysUserCreateReq;
import com.travis.monolith.system.user.api.request.SysUserPageReq;
import com.travis.monolith.system.user.api.response.SysUserResp;
import com.travis.monolith.system.user.internal.converter.SysUserConverter;
import com.travis.monolith.system.user.internal.entity.SysUser;
import com.travis.monolith.system.user.internal.mapper.SysUserMapper;
import com.travis.monolith.system.user.internal.service.impl.SysUserServiceImpl;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
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

    @Test
    void shouldBatchLoadPageAssociations() {
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
        user.setDeptId(2L);
        user.setAvatarFileId(3L);
        var response = new SysUserResp();
        response.setId(1L);
        var page = new Page<SysUser>(1, 10);
        page.setRecords(List.of(user));
        page.setTotal(1);
        when(mapper.page(any(Integer.class), any(Integer.class), any())).thenReturn(page);
        when(converter.toResp(user)).thenReturn(response);
        when(deptApi.getDeptNameMapByIds(any())).thenReturn(Map.of(2L, "研发部"));
        when(roleApi.getRoleNameMapByUserIds(List.of(1L))).thenReturn(Map.of(1L, List.of("管理员")));
        when(fileApi.getFileUrlMapByIds(any())).thenReturn(Map.of(3L, "/files/avatar.png"));
        var request = new SysUserPageReq();
        request.setPageNum(1);
        request.setPageSize(10);

        var result = service.page(request);

        assertThat(result.getRecords().getFirst().getDeptName()).isEqualTo("研发部");
        assertThat(result.getRecords().getFirst().getRoleNames()).containsExactly("管理员");
        assertThat(result.getRecords().getFirst().getAvatar()).isEqualTo("/files/avatar.png");
        verify(deptApi, never()).getDeptNameById(any());
        verify(roleApi, never()).getRoleNamesByUserId(any());
        verify(fileApi, never()).getFileUrlById(any());
    }

    private SysUserServiceImpl service(SysUserMapper mapper, SysDeptApi deptApi) {
        @SuppressWarnings("unchecked")
        ObjectProvider<
                        com.travis.infrastructure.framework.websocket.core.session
                                .WebSocketSessionManager>
                sessionManagers = mock(ObjectProvider.class);
        var service =
                new SysUserServiceImpl(
                        deptApi,
                        mock(SysRoleApi.class),
                        mock(SysFileApi.class),
                        mock(TransactionalApplicationEventPublisher.class),
                        sessionManagers,
                        mock(SysUserConverter.class));
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        return service;
    }
}
