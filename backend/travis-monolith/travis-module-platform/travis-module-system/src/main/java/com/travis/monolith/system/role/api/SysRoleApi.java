package com.travis.monolith.system.role.api;

import com.travis.monolith.system.role.api.response.SysRoleResp;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 角色模块对外 API，供跨模块调用，只暴露 DTO，不暴露 entity
 *
 * @author travis
 */
public interface SysRoleApi {

    /**
     * 根据用户ID查询其角色ID列表
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> getRoleIdsByUserId(Long userId);

    /**
     * 根据用户ID查询角色编码列表
     *
     * @param userId 用户ID
     * @return 角色编码列表
     */
    List<String> getRoleCodesByUserId(Long userId);

    /**
     * 根据用户ID查询角色名称列表
     *
     * @param userId 用户ID
     * @return 角色名称列表
     */
    List<String> getRoleNamesByUserId(Long userId);

    /**
     * 根据角色ID列表获取角色编码列表
     *
     * @param roleIds 角色ID列表
     * @return 角色编码列表
     */
    List<String> getRoleCodesByRoleIds(List<Long> roleIds);

    /**
     * 根据角色ID列表获取角色名称列表
     *
     * @param roleIds 角色ID列表
     * @return 角色名称列表
     */
    List<String> getRoleNamesByRoleIds(List<Long> roleIds);

    /**
     * 根据角色ID列表批量查询角色名称映射
     *
     * @param roleIds 角色ID集合
     * @return roleId -> roleName 映射
     */
    Map<Long, String> getRoleNameMapByIds(Set<Long> roleIds);

    /**
     * 根据角色ID列表获取关联的菜单ID列表
     *
     * @param roleIds 角色ID列表
     * @return 菜单ID列表（去重）
     */
    List<Long> getMenuIdsByRoleIds(List<Long> roleIds);

    /**
     * 删除指定用户的所有角色关联
     *
     * @param userId 用户ID
     */
    void deleteUserRolesByUserId(Long userId);

    /**
     * 为指定用户分配角色
     *
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    void assignUserRoles(Long userId, List<Long> roleIds);

    /**
     * 批量查询多个用户的角色名称映射
     *
     * @param userIds 用户ID列表
     * @return userId -> roleNameList 映射
     */
    Map<Long, List<String>> batchGetRoleNamesByUserIds(List<Long> userIds);

    /**
     * 将指定菜单自动分配给所有 admin 角色
     *
     * @param menuId 菜单ID
     */
    void assignMenuToAdminRoles(Long menuId);

    /**
     * 将指定菜单从所有 admin 角色中移除
     *
     * @param menuId 菜单ID
     */
    void removeMenuFromAdminRoles(Long menuId);

    /**
     * 获取所有启用角色列表
     *
     * @return 角色列表
     */
    List<SysRoleResp> listEnabled();

    /** 根据角色ID查询关联用户ID。 */
    List<Long> getUserIdsByRoleIds(List<Long> roleIds);
}
