package com.travis.monolith.system.role.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.role.api.request.SysRoleCreateReq;
import com.travis.monolith.system.role.api.request.SysRoleMenuReq;
import com.travis.monolith.system.role.api.request.SysRolePageReq;
import com.travis.monolith.system.role.api.request.SysRoleUpdateReq;
import com.travis.monolith.system.role.api.response.SysRoleResp;
import com.travis.monolith.system.role.internal.entity.SysRole;
import java.util.List;

/**
 * 角色管理服务接口，提供角色的增删改查、菜单分配及角色信息查询
 *
 * @author travis
 */
public interface SysRoleService extends IService<SysRole> {

    /**
     * 分页查询角色列表
     *
     * @param req 分页查询参数
     * @return 分页结果
     */
    PageResp<SysRoleResp> page(SysRolePageReq req);

    /**
     * 获取角色详情，包含已分配的菜单ID列表
     *
     * @param id 角色ID
     * @return 角色详情视图
     */
    SysRoleResp getDetailByIdOrThrow(Long id);

    /**
     * 新增角色
     *
     * @param req 角色信息请求参数
     */
    void create(SysRoleCreateReq req);

    /**
     * 更新角色信息
     *
     * @param id 角色ID
     * @param req 角色信息请求参数
     */
    void update(Long id, SysRoleUpdateReq req);

    /** 修改角色状态 */
    void updateStatus(Long id, Integer status);

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
     * 根据角色ID获取角色编码
     *
     * @param roleId 角色ID
     * @return 角色编码
     */
    String getRoleCodeByRoleId(Long roleId);

    /**
     * 根据角色ID获取角色名称
     *
     * @param roleId 角色ID
     * @return 角色名称
     */
    String getRoleNameByRoleId(Long roleId);

    /** 批量查询角色名称，返回顺序与存在的输入角色 ID 一致。 */
    List<String> getRoleNamesByRoleIds(List<Long> roleIds);

    /**
     * 根据角色ID获取关联的菜单ID列表
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    List<Long> getMenuIdsByRoleId(Long roleId);

    /**
     * 将指定菜单自动分配给所有 admin 角色
     *
     * @param menuId 菜单ID
     */
    void assignMenuToAdminRoles(Long menuId);

    /**
     * 删除指定菜单的所有角色关联
     *
     * @param menuIds 菜单ID列表
     */
    void removeMenuRelations(List<Long> menuIds);

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
}
