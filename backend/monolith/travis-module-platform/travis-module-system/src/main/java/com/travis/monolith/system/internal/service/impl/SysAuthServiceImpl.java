package com.travis.monolith.system.internal.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travis.infrastructure.framework.web.core.exception.BizException;
import com.travis.infrastructure.framework.web.core.exception.CommonErrorCode;
import com.travis.monolith.system.internal.exception.SystemErrorCode;
import com.travis.monolith.system.internal.mapper.SysMenuMapper;
import com.travis.monolith.system.internal.mapper.SysRoleMenuMapper;
import com.travis.monolith.system.internal.model.entity.SysMenu;
import com.travis.monolith.system.internal.model.entity.SysRoleMenu;
import com.travis.monolith.system.internal.model.entity.SysUser;
import com.travis.monolith.system.internal.model.req.SysUserLoginReq;
import com.travis.monolith.system.internal.model.resp.SysUserLoginResp;
import com.travis.monolith.system.internal.model.resp.UserInfoResp;
import com.travis.monolith.system.internal.model.resp.VbenMenuResp;
import com.travis.monolith.system.internal.service.SysAuthService;
import com.travis.monolith.system.internal.service.SysMenuService;
import com.travis.monolith.system.internal.service.SysRoleService;
import com.travis.monolith.system.internal.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 后台认证服务实现，处理登录验证（BCrypt 密码校验）、用户信息获取及权限查询
 *
 * @author travis
 */
@Service
@RequiredArgsConstructor
public class SysAuthServiceImpl implements SysAuthService {

    /**
     * 用户管理服务
     */
    private final SysUserService userService;
    /**
     * 角色管理服务
     */
    private final SysRoleService roleService;
    /**
     * 菜单管理服务
     */
    private final SysMenuService menuService;
    /**
     * 菜单 Mapper
     */
    private final SysMenuMapper menuMapper;
    /**
     * 角色-菜单关联 Mapper
     */
    private final SysRoleMenuMapper roleMenuMapper;

    /**
     * 管理员登录：校验用户名密码、账号状态，通过后使用 Sa-Token 签发令牌
     */
    @Override
    public SysUserLoginResp login(SysUserLoginReq req) {
        // 显式查询密码字段（实体中 password 标记了 select=false，默认不返回）
        var user = userService.lambdaQuery()
                .eq(SysUser::getUsername, req.getUsername())
                .select(SysUser::getId, SysUser::getUsername, SysUser::getPassword,
                        SysUser::getStatus)
                .one();
        if (user == null) {
            throw new BizException(SystemErrorCode.SYSTEM_AUTH_LOGIN_BAD_CREDENTIALS);
        }
        // 检查账号是否被禁用
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException(SystemErrorCode.SYSTEM_AUTH_LOGIN_USER_DISABLED);
        }

        // BCrypt 校验密码
        if (!BCrypt.checkpw(req.getPassword(), user.getPassword())) {
            throw new BizException(SystemErrorCode.SYSTEM_AUTH_LOGIN_BAD_CREDENTIALS);
        }

        // 校验通过，通过 Sa-Token 执行登录
        StpUtil.login(user.getId());
        var token = StpUtil.getTokenValue();

        return SysUserLoginResp.builder()
                .accessToken(token)
                .refreshToken(token)
                .build();
    }

    /**
     * 获取当前登录用户信息，包含角色编码和权限列表
     */
    @Override
    public UserInfoResp getUserInfo() {
        long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userService.getById(userId);
        if (user == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }

        List<String> roleCodes = roleService.getRoleCodesByUserId(userId);
        List<String> permissions = getPermissionsByUserId(userId);

        return UserInfoResp.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getNickname())
                .avatar(user.getAvatar())
                .roles(roleCodes)
                .permissions(permissions)
                .homePath("/dashboard")
                .build();
    }

    /**
     * 获取当前用户的菜单树（用于前端路由渲染）
     */
    @Override
    public List<VbenMenuResp> getMenuList() {
        long userId = StpUtil.getLoginIdAsLong();
        List<Long> roleIds = roleService.getRoleIdsByUserId(userId);
        return menuService.getVbenMenuTree(roleIds);
    }

    /**
     * 获取当前用户的权限标识列表（用于前端按钮级权限控制）
     */
    @Override
    public List<String> getAccessCodes() {
        long userId = StpUtil.getLoginIdAsLong();
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

        // 通过角色ID查询关联的菜单ID
        List<Long> menuIds = roleMenuMapper.selectList(
                        new LambdaQueryWrapper<SysRoleMenu>()
                                .in(SysRoleMenu::getRoleId, roleIds))
                .stream()
                .map(SysRoleMenu::getMenuId)
                .distinct()
                .collect(Collectors.toList());

        if (menuIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 查询菜单中有权限标识且状态启用的 perms 字段
        return menuMapper.selectList(
                        new LambdaQueryWrapper<SysMenu>()
                                .in(SysMenu::getId, menuIds)
                                .isNotNull(SysMenu::getPerms)
                                .ne(SysMenu::getPerms, "")
                                .eq(SysMenu::getStatus, 1))
                .stream()
                .map(SysMenu::getPerms)
                .distinct()
                .collect(Collectors.toList());
    }
}
