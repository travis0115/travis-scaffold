package com.travis.monolith.system.user.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.user.api.request.*;
import com.travis.monolith.system.user.api.response.SysUserDashboardResp;
import com.travis.monolith.system.user.api.response.SysUserResp;
import com.travis.monolith.system.user.internal.entity.SysUser;
import java.util.Collection;
import java.util.Map;

/**
 * 用户管理服务接口，提供管理员的分页查询、增删改查及角色分配
 *
 * @author travis
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 分页查询用户列表
     *
     * @param req 分页查询参数
     * @return 分页结果
     */
    PageResp<SysUserResp> page(SysUserPageReq req);

    /**
     * 获取用户详情，包含关联的角色信息
     *
     * @param id 用户ID
     * @return 用户详情视图
     */
    SysUserResp getDetailByIdOrThrow(Long id);

    /**
     * 新增用户
     *
     * @param req 用户信息请求参数
     * @return 新建用户ID
     */
    Long create(SysUserCreateReq req);

    /**
     * 更新用户信息
     *
     * @param id 用户ID
     * @param req 用户信息请求参数
     */
    void update(Long id, SysUserUpdateReq req);

    /** 修改用户状态 */
    void updateStatus(Long id, Integer status);

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    void deleteById(Long id);

    /**
     * 批量重置指定部门下的用户部门归属
     *
     * @param deptIds 部门ID集合
     */
    void resetDeptByDeptIds(Collection<Long> deptIds);

    /**
     * 标记用户上线
     *
     * @param userId 用户ID
     * @param ip 上线IP
     */
    void markOnline(Long userId, String ip);

    /**
     * 标记用户下线
     *
     * @param userId 用户ID
     */
    void markOffline(Long userId);

    /**
     * 获取在线用户数量
     *
     * @return 在线用户数量
     */
    Long countOnlineUsers();

    /** 获取首页用户概览。 */
    SysUserDashboardResp dashboard();

    /**
     * 为用户分配角色（先清除原有关联再批量插入）
     *
     * @param req 用户角色分配请求参数
     */
    void assignRoles(SysUserRoleReq req);

    /**
     * 根据用户ID查询用户名
     *
     * @param userId 用户ID
     * @return 用户名，不存在返回 null
     */
    String getUsernameById(Long userId);

    /** 批量查询用户 ID 与用户名。 */
    Map<Long, String> getUsernameMapByIds(Collection<Long> userIds);

    /**
     * 当前登录用户修改个人资料
     *
     * @param req 个人资料请求参数
     */
    void updateProfile(SysUserProfileReq req);

    /** 当前登录用户更新头像 */
    void updateAvatar(Long avatarFileId);

    /**
     * 当前登录用户修改密码
     *
     * @param req 修改密码请求参数
     */
    void changePassword(SysUserChangePasswordReq req);

    /**
     * 重置用户密码
     *
     * @param id 用户ID
     * @param newPassword 新密码（可选，为null时自动生成随机密码）
     * @return 最终使用的密码（明文，供管理员转达用户）
     */
    String resetPassword(Long id, String newPassword);
}
