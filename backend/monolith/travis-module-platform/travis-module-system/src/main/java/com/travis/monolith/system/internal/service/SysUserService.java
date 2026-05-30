package com.travis.monolith.system.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.framework.web.core.model.PageResult;
import com.travis.monolith.system.internal.model.entity.SysUser;
import com.travis.monolith.system.internal.model.req.SysUserReq;
import com.travis.monolith.system.internal.model.req.SysUserRoleReq;
import com.travis.monolith.system.internal.model.resp.SysUserResp;

/**
 * 用户管理服务接口，提供管理员的分页查询、增删改查及角色分配
 *
 * @author travis
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 分页查询用户列表
     *
     * @param username 用户名（模糊匹配，可为空）
     * @param phone    手机号（模糊匹配，可为空）
     * @param status   状态（可为空）
     * @param deptId   所属部门ID（可为空）
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageResult<SysUserResp> getUserPage(String username, String phone, Integer status, Long deptId, Integer pageNum, Integer pageSize);

    /**
     * 获取用户详情，包含关联的角色信息
     *
     * @param id 用户ID
     * @return 用户详情视图
     */
    SysUserResp getUserDetail(Long id);

    /**
     * 新增用户
     *
     * @param req 用户信息请求参数
     */
    void addUser(SysUserReq req);

    /**
     * 更新用户信息
     *
     * @param id  用户ID
     * @param req 用户信息请求参数
     */
    void updateUser(Long id, SysUserReq req);

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    void deleteUser(Long id);

    /**
     * 为用户分配角色（先清除原有关联再批量插入）
     *
     * @param req 用户角色分配请求参数
     */
    void assignRoles(SysUserRoleReq req);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体
     */
    SysUser getUserByUsername(String username);
}
