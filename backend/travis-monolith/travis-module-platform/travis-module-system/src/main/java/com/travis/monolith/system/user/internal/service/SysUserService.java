package com.travis.monolith.system.user.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResult;
import com.travis.monolith.system.user.internal.entity.SysUser;
import com.travis.monolith.system.user.api.model.request.ChangePasswordReq;
import com.travis.monolith.system.user.api.model.request.SysUserReq;
import com.travis.monolith.system.user.api.model.request.SysUserRoleReq;
import com.travis.monolith.system.user.api.model.request.UpdateAvatarReq;
import com.travis.monolith.system.user.api.model.request.UserProfileReq;
import com.travis.monolith.system.user.api.model.response.SysUserResp;

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
     * @param phone 手机号（模糊匹配，可为空）
     * @param status 状态（可为空）
     * @param deptId 所属部门ID（可为空）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageResult<SysUserResp> getUserPage(
            String username,
            String phone,
            Integer status,
            Long deptId,
            Integer pageNum,
            Integer pageSize);

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
     * @return 新建用户ID
     */
    Long addUser(SysUserReq req);

    /**
     * 更新用户信息
     *
     * @param id 用户ID
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

    /**
     * 当前登录用户修改个人资料
     *
     * @param req 个人资料请求参数
     */
    void updateProfile(UserProfileReq req);

    /**
     * 当前登录用户更新头像
     *
     * @param req 头像更新请求参数
     */
    void updateAvatar(UpdateAvatarReq req);

    /**
     * 当前登录用户修改密码
     *
     * @param req 修改密码请求参数
     */
    void changePassword(ChangePasswordReq req);

    /**
     * 重置用户密码
     *
     * @param id 用户ID
     * @param newPassword 新密码（可选，为null时自动生成随机密码）
     * @return 最终使用的密码（明文，供管理员转达用户）
     */
    String resetPassword(Long id, String newPassword);
}
