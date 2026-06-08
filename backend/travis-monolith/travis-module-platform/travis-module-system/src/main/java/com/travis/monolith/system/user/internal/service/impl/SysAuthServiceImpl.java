package com.travis.monolith.system.user.internal.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.travis.infrastructure.common.web.enums.LoginType;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.framework.satoken.core.StpKit;
import com.travis.infrastructure.framework.web.core.exception.BizException;
import com.travis.infrastructure.framework.web.core.util.IpUtil;
import com.travis.monolith.system.file.api.SysFileService;
import com.travis.monolith.system.user.api.event.UserLoginEvent;
import com.travis.monolith.system.menu.api.SysMenuService;
import com.travis.monolith.system.menu.api.model.VbenMenuResp;
import com.travis.monolith.system.role.api.SysRoleService;
import com.travis.monolith.system.user.internal.service.SysAuthService;
import com.travis.monolith.system.user.internal.service.SysUserService;
import com.travis.monolith.system.user.api.model.request.SysUserLoginReq;
import com.travis.monolith.system.user.api.model.response.SysUserLoginResp;
import com.travis.monolith.system.user.api.model.response.UserInfoResp;
import com.travis.monolith.system.user.internal.entity.SysUser;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 后台认证服务实现，处理登录验证（BCrypt 密码校验）、用户信息获取及权限查询
 *
 * @author travis
 */
@Service
@RequiredArgsConstructor
public class SysAuthServiceImpl implements SysAuthService {

    /** 文件服务 */
    private final SysFileService fileService;

    /** 用户管理服务 */
    private final SysUserService userService;

    /** 角色管理服务 */
    private final SysRoleService roleService;

    /** 菜单管理服务 */
    private final SysMenuService menuService;

    /** Spring 事件发布器 */
    private final ApplicationEventPublisher eventPublisher;

    /** 管理员登录：校验用户名密码、账号状态，通过后使用 Sa-Token 签发令牌，并记录登录日志 */
    @Override
    public SysUserLoginResp login(SysUserLoginReq req) {
        // 显式查询密码字段（实体中 password 标记了 select=false，默认不返回）
        var user =
                userService
                        .lambdaQuery()
                        .eq(SysUser::getUsername, req.getUsername())
                        .select(
                                SysUser::getId,
                                SysUser::getUsername,
                                SysUser::getPassword,
                                SysUser::getStatus)
                        .one();
        if (user == null) {
            eventPublisher.publishEvent(new UserLoginEvent(req.getUsername(), 0, "用户不存在"));
            throw new BizException(CommonErrorCode.AUTH_LOGIN_BAD_CREDENTIALS);
        }
        // 检查账号是否被禁用
        if (user.getStatus() != null && user.getStatus() == 0) {
            eventPublisher.publishEvent(new UserLoginEvent(req.getUsername(), 0, "账号已被禁用"));
            throw new BizException(CommonErrorCode.AUTH_LOGIN_USER_DISABLED);
        }

        // BCrypt 校验密码
        if (!BCrypt.checkpw(req.getPassword(), user.getPassword())) {
            eventPublisher.publishEvent(new UserLoginEvent(req.getUsername(), 0, "密码错误"));
            throw new BizException(CommonErrorCode.AUTH_LOGIN_BAD_CREDENTIALS);
        }

        // 校验通过，通过 Sa-Token 执行登录
        StpKit.of(LoginType.ADMIN).login(user.getId());
        var token = StpKit.of(LoginType.ADMIN).getTokenValue();

        // 更新最后登录时间和IP
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(IpUtil.getClientIp());
        userService.updateById(user);

        // 记录登录成功日志
        eventPublisher.publishEvent(new UserLoginEvent(req.getUsername(), 1, "登录成功"));

        return SysUserLoginResp.builder().accessToken(token).refreshToken(token).build();
    }

    /** 获取当前登录用户信息，包含角色编码和权限列表 */
    @Override
    public UserInfoResp getUserInfo() {
        long userId = StpKit.of(LoginType.ADMIN).getLoginIdAsLong();
        SysUser user = userService.getById(userId);
        if (user == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }

        List<String> roleCodes = roleService.getRoleCodesByUserId(userId);
        List<String> permissions = getPermissionsByUserId(userId);
        List<String> roleNames = roleService.getRoleNamesByUserId(userId);

        return UserInfoResp.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(fileService.getFileUrl(user.getAvatar()))
                .email(user.getEmail())
                .mobile(user.getMobile())
                .roles(roleCodes)
                .roleNames(roleNames)
                .permissions(permissions)
                .homePath("/")
                .build();
    }

    /** 获取当前用户的菜单树（用于前端路由渲染），按用户ID缓存 */
    @Override
    @Cacheable(
            value = "menus:vben",
            key =
                    "T(com.travis.infrastructure.framework.satoken.core.StpKit).getLoginIdAsLong(T(com.travis.infrastructure.common.web.enums.LoginType).ADMIN)")
    public List<VbenMenuResp> getMenuList() {
        long userId = StpKit.of(LoginType.ADMIN).getLoginIdAsLong();
        List<Long> roleIds = roleService.getRoleIdsByUserId(userId);
        return menuService.getVbenMenuTree(roleIds);
    }

    /** 获取当前用户的权限标识列表（用于前端按钮级权限控制） */
    @Override
    public List<String> getAccessCodes() {
        Long userId = StpKit.of(LoginType.ADMIN).getLoginIdAsLong();
        return getAccessCodes(userId);
    }

    /** 获取当前用户的权限标识列表（用于前端按钮级权限控制） */
    @Override
    public List<String> getAccessCodes(Long userId) {
        return getPermissionsByUserId(userId);
    }

    /**
     * 根据用户ID查询其所有权限标识：用户 -> 角色 -> 角色菜单 -> 菜单权限标识
     *
     * @param userId 用户ID
     * @return 权限标识列表（去重）
     */
    private List<String> getPermissionsByUserId(Long userId) {
        List<Long> roleIds = roleService.getRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 通过角色服务查询关联的菜单ID
        List<Long> menuIds = roleService.getMenuIdsByRoleIds(roleIds);
        if (menuIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 通过菜单服务查询权限标识
        return menuService.getPermissionsByMenuIds(menuIds);
    }
}
