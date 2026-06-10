package com.travis.monolith.system.role.internal.api;

import com.travis.monolith.system.role.api.SysRoleApi;
import com.travis.monolith.system.role.api.response.SysRoleResp;
import com.travis.monolith.system.role.internal.service.SysRoleService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 角色模块对外 API 实现，委托调用内部 Service
 *
 * @author travis
 */
@Component
@RequiredArgsConstructor
public class SysRoleApiImpl implements SysRoleApi {

    private final SysRoleService roleService;

    @Override
    public List<Long> getRoleIdsByUserId(Long userId) {
        return roleService.getRoleIdsByUserId(userId);
    }

    @Override
    public List<String> getRoleCodesByUserId(Long userId) {
        return roleService.getRoleCodesByUserId(userId);
    }

    @Override
    public List<String> getRoleNamesByUserId(Long userId) {
        return roleService.getRoleNamesByUserId(userId);
    }

    @Override
    public List<String> getRoleCodesByRoleIds(List<Long> roleIds) {
        return roleService.getRoleCodesByRoleIds(roleIds);
    }

    @Override
    public List<String> getRoleNamesByRoleIds(List<Long> roleIds) {
        return roleService.getRoleNamesByRoleIds(roleIds);
    }

    @Override
    public Map<Long, String> getRoleNameMapByIds(Set<Long> roleIds) {
        return roleService.getRoleNameMapByIds(roleIds);
    }

    @Override
    public List<Long> getMenuIdsByRoleIds(List<Long> roleIds) {
        return roleService.getMenuIdsByRoleIds(roleIds);
    }

    @Override
    public void deleteUserRolesByUserId(Long userId) {
        roleService.deleteUserRolesByUserId(userId);
    }

    @Override
    public void assignUserRoles(Long userId, List<Long> roleIds) {
        roleService.assignUserRoles(userId, roleIds);
    }

    @Override
    public Map<Long, List<String>> batchGetRoleNamesByUserIds(List<Long> userIds) {
        return roleService.batchGetRoleNamesByUserIds(userIds);
    }

    @Override
    public void assignMenuToAdminRoles(Long menuId) {
        roleService.assignMenuToAdminRoles(menuId);
    }

    @Override
    public void removeMenuFromAdminRoles(Long menuId) {
        roleService.removeMenuFromAdminRoles(menuId);
    }

    @Override
    public List<SysRoleResp> listEnabled() {
        return roleService.listEnabled();
    }
}
