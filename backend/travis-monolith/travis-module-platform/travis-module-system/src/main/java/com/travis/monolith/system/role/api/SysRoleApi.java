package com.travis.monolith.system.role.api;

import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.validation.annotation.Validated;

/**
 * 角色模块对外 API，供跨模块调用，只暴露 DTO，不暴露 entity
 *
 * @author travis
 */
@Validated
public interface SysRoleApi {

    /**
     * 根据用户ID查询其角色ID列表
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> getRoleIdsByUserId(@NotNull(message = "用户ID不能为空") Long userId);

    /**
     * 根据用户ID查询角色编码列表
     *
     * @param userId 用户ID
     * @return 角色编码列表
     */
    List<String> getRoleCodesByUserId(@NotNull(message = "用户ID不能为空") Long userId);

    /**
     * 根据用户ID查询角色名称列表
     *
     * @param userId 用户ID
     * @return 角色名称列表
     */
    List<String> getRoleNamesByUserId(@NotNull(message = "用户ID不能为空") Long userId);

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

    /** 查询拥有指定角色的用户 ID。 */
    Set<Long> getUserIdsByRoleIds(Collection<Long> roleIds);

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
    void deleteUserRolesByUserId(@NotNull(message = "用户ID不能为空") Long userId);

    /**
     * 为指定用户分配角色
     *
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    void assignUserRoles(@NotNull(message = "用户ID不能为空") Long userId, List<Long> roleIds);

    /**
     * 将指定菜单自动分配给所有 admin 角色
     *
     * @param menuId 菜单ID
     */
    void assignMenuToAdminRoles(@NotNull(message = "菜单ID不能为空") Long menuId);

    /**
     * 删除指定菜单的所有角色关联
     *
     * @param menuIds 菜单ID列表
     */
    void removeMenuRelations(List<Long> menuIds);
}
