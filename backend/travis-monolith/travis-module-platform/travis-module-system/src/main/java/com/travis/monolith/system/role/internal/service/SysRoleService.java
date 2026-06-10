package com.travis.monolith.system.role.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResult;
import com.travis.monolith.system.role.api.request.SysRoleMenuReq;
import com.travis.monolith.system.role.api.response.SysRoleResp;
import com.travis.monolith.system.role.internal.entity.SysRole;
import com.travis.monolith.system.role.internal.request.SysRoleReq;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 角色管理服务接口，提供角色的增删改查、菜单分配及角色信息查询
 *
 * @author travis
 */
public interface SysRoleService extends IService<SysRole> {

    /**
     * 分页查询角色列表
     *
     * @param roleName 角色名称（模糊匹配，可为空）
     * @param roleCode 角色编码（模糊匹配，可为空）
     * @param status 状态（可为空）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageResult<SysRoleResp> page(
            String roleName, String roleCode, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 获取角色详情，包含已分配的菜单ID列表
     *
     * @param id 角色ID
     * @return 角色详情视图
     */
    SysRoleResp getById(Long id);

    /**
     * 新增角色
     *
     * @param req 角色信息请求参数
     */
    void create(SysRoleReq req);

    /**
     * 更新角色信息
     *
     * @param id 角色ID
     * @param req 角色信息请求参数
     */
    void update(Long id, SysRoleReq req);

    /**
     * 删除角色
     *
     * @param id 角色ID
     */
    void deleteById(Long id);

    /**
     * 为角色分配菜单权限（先清除原有关联再批量插入）
     *
     * @param req 角色菜单分配请求参数
     */
    void assignMenus(SysRoleMenuReq req);

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
     * 根据角色ID列表批量查询角色名称映射（roleId -> roleName）
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
     * 获取所有启用角色列表（不分页）
     *
     * @return 角色列表
     */
    List<SysRoleResp> listEnabled();

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
     * 删除指定用户的所有角色关联
     *
     * @param userId 用户ID
     */
    void deleteUserRolesByUserId(Long userId);

    /**
     * 为指定用户分配角色（先清除原有关联再批量插入）
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
}
