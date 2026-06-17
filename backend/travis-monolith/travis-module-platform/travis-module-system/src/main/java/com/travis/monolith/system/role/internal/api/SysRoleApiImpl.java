package com.travis.monolith.system.role.internal.api;

import com.travis.monolith.system.role.api.SysRoleApi;
import com.travis.monolith.system.role.api.response.SysRoleResp;
import com.travis.monolith.system.role.internal.service.SysRoleService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    public String getRoleCodeByRoleId(Long roleId) {
        return roleService.getRoleCodeByRoleId(roleId);
    }

    @Override
    public List<String> getRoleNamesByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        return roleIds.stream()
                .map(roleService::getRoleNameByRoleId)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public String getRoleNameByRoleId(Long roleId) {
        return roleService.getRoleNameByRoleId(roleId);
    }

    @Override
    public Map<Long, String> getRoleNameMapByIds(Set<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> result = new LinkedHashMap<>();
        for (Long roleId : roleIds) {
            var roleName = roleService.getRoleNameByRoleId(roleId);
            if (roleName != null) {
                result.put(roleId, roleName);
            }
        }
        return result;
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
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        return roleService.getMenuIdsByRoleId(roleId);
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
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<String>> result = new LinkedHashMap<>();
        for (Long userId : userIds) {
            result.put(userId, getRoleNamesByRoleIds(roleService.getRoleIdsByUserId(userId)));
        }
        return result;
    }

    @Override
    public void assignMenuToAdminRoles(Long menuId) {
        roleService.assignMenuToAdminRoles(menuId);
    }

    @Override
    public void removeMenuRelations(List<Long> menuIds) {
        roleService.removeMenuRelations(menuIds);
    }

    @Override
    public List<SysRoleResp> listEnabled() {
        return roleService.listEnabled();
    }

    @Override
    public List<Long> getUserIdsByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        return roleIds.stream()
                .flatMap(roleId -> roleService.getUserIdsByRoleId(roleId).stream())
                .distinct()
                .toList();
    }

    @Override
    public List<Long> getUserIdsByRoleId(Long roleId) {
        return roleService.getUserIdsByRoleId(roleId);
    }
}
