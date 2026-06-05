package com.travis.monolith.system.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.framework.web.core.model.PageResult;
import com.travis.monolith.system.internal.model.entity.SysRole;
import com.travis.monolith.system.internal.model.request.role.SysRoleMenuReq;
import com.travis.monolith.system.internal.model.request.role.SysRoleReq;
import com.travis.monolith.system.internal.model.response.role.SysRoleResp;

import java.util.List;

/**
 * 角色管理服务接口，提供角色的增删改查、菜单分配及用户角色关联查询
 *
 * @author travis
 */
public interface SysRoleService extends IService<SysRole> {

    /**
     * 分页查询角色列表
     *
     * @param roleName  角色名称（模糊匹配，可为空）
     * @param roleCode  角色编码（模糊匹配，可为空）
     * @param status    状态（可为空）
     * @param pageNum   页码
     * @param pageSize  每页条数
     * @return 分页结果
     */
    PageResult<SysRoleResp> getRolePage(String roleName, String roleCode, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 获取角色详情，包含已分配的菜单ID列表
     *
     * @param id 角色ID
     * @return 角色详情视图
     */
    SysRoleResp getRoleDetail(Long id);

    /**
     * 新增角色
     *
     * @param req 角色信息请求参数
     */
    void addRole(SysRoleReq req);

    /**
     * 更新角色信息
     *
     * @param id  角色ID
     * @param req 角色信息请求参数
     */
    void updateRole(Long id, SysRoleReq req);

    /**
     * 删除角色
     *
     * @param id 角色ID
     */
    void deleteRole(Long id);

    /**
     * 为角色分配菜单权限（先清除原有关联再批量插入）
     *
     * @param req 角色菜单分配请求参数
     */
    void assignMenus(SysRoleMenuReq req);

    /**
     * 根据用户ID查询其角色编码列表
     *
     * @param userId 用户ID
     * @return 角色编码列表
     */
    List<String> getRoleCodesByUserId(Long userId);

    /**
     * 根据用户ID查询其角色ID列表
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> getRoleIdsByUserId(Long userId);

    /**
     * 根据用户ID查询角色名称列表
     *
     * @param userId 用户ID
     * @return 角色名称列表
     */
    List<String> getRoleNamesByUserId(Long userId);

    /**
     * 获取所有启用角色列表（不分页）
     *
     * @return 角色列表
     */
    List<SysRoleResp> getEnabledRoleList();
}
