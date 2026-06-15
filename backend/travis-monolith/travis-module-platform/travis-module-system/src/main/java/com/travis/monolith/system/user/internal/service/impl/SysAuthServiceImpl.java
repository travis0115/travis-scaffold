package com.travis.monolith.system.user.internal.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.travis.infrastructure.common.event.MessagePublisher;
import com.travis.infrastructure.common.web.enums.LoginType;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.framework.satoken.core.StpKit;
import com.travis.infrastructure.framework.web.core.model.UserAgentInfo;
import com.travis.infrastructure.framework.web.core.util.IpUtil;
import com.travis.infrastructure.framework.web.core.util.UserAgentUtil;
import com.travis.monolith.system.common.api.SystemEvent;
import com.travis.monolith.system.menu.api.SysMenuApi;
import com.travis.monolith.system.menu.api.response.VbenMenuResp;
import com.travis.monolith.system.role.api.SysRoleApi;
import com.travis.monolith.system.user.api.event.UserLoginPayload;
import com.travis.monolith.system.user.api.request.SysUserLoginReq;
import com.travis.monolith.system.user.api.response.SysUserDetailResp;
import com.travis.monolith.system.user.api.response.SysUserLoginResp;
import com.travis.monolith.system.user.api.response.UserInfoResp;
import com.travis.monolith.system.user.internal.entity.SysUser;
import com.travis.monolith.system.user.internal.service.SysAuthService;
import com.travis.monolith.system.user.internal.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class SysAuthServiceImpl implements SysAuthService {

    /** 用户管理服务 */
    private final SysUserService userService;

    /** 角色 API */
    private final SysRoleApi roleApi;

    /** 菜单 API */
    private final SysMenuApi menuApi;

    /** 消息发布器 */
    private final MessagePublisher messagePublisher;

    /** 管理员登录：校验用户名密码、账号状态，通过后使用 Sa-Token 签发令牌，并记录登录日志 */
    @Override
    public SysUserLoginResp login(SysUserLoginReq req) {
        // 在 Web 线程中提前捕获请求上下文信息
        String clientIp = IpUtil.getClientIp();
        UserAgentInfo uaInfo = UserAgentUtil.getCurrentUserAgentInfo();

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
            publishLoginEvent(buildLoginPayload(req.getUsername(), 0, "用户不存在", clientIp, uaInfo));
            throw new BizException(CommonErrorCode.AUTH_LOGIN_BAD_CREDENTIALS);
        }
        // 检查账号是否被禁用
        if (user.getStatus() != null && user.getStatus() == 0) {
            publishLoginEvent(buildLoginPayload(req.getUsername(), 0, "账号已被禁用", clientIp, uaInfo));
            throw new BizException(CommonErrorCode.AUTH_LOGIN_USER_DISABLED);
        }

        // BCrypt 校验密码
        if (!BCrypt.checkpw(req.getPassword(), user.getPassword())) {
            publishLoginEvent(buildLoginPayload(req.getUsername(), 0, "密码错误", clientIp, uaInfo));
            throw new BizException(CommonErrorCode.AUTH_LOGIN_BAD_CREDENTIALS);
        }

        // 校验通过，通过 Sa-Token 执行登录
        StpKit.of(LoginType.ADMIN).login(user.getId());
        var token = StpKit.of(LoginType.ADMIN).getTokenValue();

        // 更新最后登录时间和IP
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(clientIp);
        userService.updateById(user);

        // 记录登录成功日志
        publishLoginEvent(buildLoginPayload(req.getUsername(), 1, "登录成功", clientIp, uaInfo));

        return SysUserLoginResp.builder().accessToken(token).refreshToken(token).build();
    }

    private void publishLoginEvent(UserLoginPayload payload) {
        try {
            messagePublisher.asyncPublish(
                    SystemEvent.USER_LOGIN,
                    payload,
                    (event, body, options, ex) -> {
                        if (ex != null) {
                            log.error("登录日志事件发送失败, username={}", payload.username(), ex);
                        }
                    });
        } catch (RuntimeException e) {
            log.error("登录日志事件发送失败, username={}", payload.username(), e);
        }
    }

    /** 获取当前登录用户信息，包含角色编码和权限列表 */
    @Override
    public UserInfoResp getUserInfo() {
        long userId = StpKit.of(LoginType.ADMIN).getLoginIdAsLong();
        SysUserDetailResp user = userService.getById(userId);
        if (user == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }

        List<String> roleCodes = roleApi.getRoleCodesByUserId(userId);
        List<String> permissions = getPermissionsByUserId(userId);
        List<String> roleNames = roleApi.getRoleNamesByUserId(userId);

        return UserInfoResp.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
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
    public List<VbenMenuResp> listMenus() {
        long userId = StpKit.of(LoginType.ADMIN).getLoginIdAsLong();
        return menuApi.getVbenMenuTree(userId);
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

    private static UserLoginPayload buildLoginPayload(
            String username, int status, String message, String ip, UserAgentInfo uaInfo) {
        return UserLoginPayload.builder()
                .username(username)
                .status(status)
                .message(message)
                .ip(ip)
                .browser(uaInfo.getBrowser())
                .os(uaInfo.getOs())
                .build();
    }

    /**
     * 根据用户ID查询其所有权限标识：用户 -> 角色 -> 角色菜单 -> 菜单权限标识
     *
     * @param userId 用户ID
     * @return 权限标识列表（去重）
     */
    private List<String> getPermissionsByUserId(Long userId) {
        List<Long> roleIds = roleApi.getRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 通过角色 API 查询关联的菜单ID
        List<Long> menuIds = roleApi.getMenuIdsByRoleIds(roleIds);
        if (menuIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 通过菜单 API 查询权限标识
        return menuApi.getPermissionsByMenuIds(menuIds);
    }
}
