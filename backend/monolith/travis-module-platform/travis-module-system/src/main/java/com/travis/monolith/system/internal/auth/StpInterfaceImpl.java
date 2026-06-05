package com.travis.monolith.system.internal.auth;

import com.travis.monolith.system.internal.service.SysRoleService;
import com.travis.monolith.system.internal.service.SysAuthService;
import cn.dev33.satoken.stp.StpInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 权限/角色加载实现，从数据库查询当前用户的权限标识和角色编码。
 * 注意：Sa-Token 的 StpInterface 接口方法签名带有 loginId 和 loginType 参数，
 * 但当前实现统一从 StpUtil 获取当前登录用户信息，忽略这些参数。
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysRoleService roleService;
    private final SysAuthService sysAuthService;

    /**
     * 获取当前登录用户的权限标识列表
     *
     * @param loginId   账号id（当前实现忽略此参数，从 StpUtil 获取当前会话）
     * @param loginType 账号类型（当前实现忽略此参数）
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        try {
            return sysAuthService.getAccessCodes();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * 获取当前登录用户的角色编码列表
     *
     * @param loginId   账号id（当前实现忽略此参数，从 StpUtil 获取当前会话）
     * @param loginType 账号类型（当前实现忽略此参数）
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        try {
            return roleService.getRoleCodesByUserId(Long.parseLong(loginId.toString()));
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
