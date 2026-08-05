package com.travis.monolith.system.role.internal.api;

import com.travis.monolith.system.role.api.SysRoleApi;
import com.travis.monolith.system.role.internal.service.SysRoleService;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
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
        return getRoleCodesByRoleIds(roleService.getRoleIdsByUserId(userId));
    }

    @Override
    public List<String> getRoleNamesByUserId(Long userId) {
        return getRoleNamesByRoleIds(roleService.getRoleIdsByUserId(userId));
    }

    @Override
    public List<String> getRoleCodesByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        return roleIds.stream()
                .map(roleService::getRoleCodeByRoleId)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<String> getRoleNamesByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        return roleService.getRoleNamesByRoleIds(roleIds);
    }

    @Override
    public Set<Long> getUserIdsByRoleIds(Collection<Long> roleIds) {
        return roleService.getUserIdsByRoleIds(roleIds);
    }

    @Override
    public List<Long> getMenuIdsByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        return roleIds.stream()
                .flatMap(roleId -> roleService.getMenuIdsByRoleId(roleId).stream())
                .distinct()
                .toList();
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
    public void assignMenuToAdminRoles(Long menuId) {
        roleService.assignMenuToAdminRoles(menuId);
    }

    @Override
    public void removeMenuRelations(List<Long> menuIds) {
        roleService.removeMenuRelations(menuIds);
    }
}
