package com.travis.monolith.system.internal.auth;

import cn.dev33.satoken.stp.StpInterface;
import com.travis.infrastructure.common.web.enums.LoginType;
import com.travis.monolith.system.internal.service.SysAuthService;
import com.travis.monolith.system.internal.service.SysRoleService;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Sa-Token 权限/角色加载实现，从数据库查询当前用户的权限标识和角色编码。 注意：Sa-Token 的 StpInterface 接口方法签名带有 loginId 和 loginType
 * 参数， 但当前实现统一从 StpUtil 获取当前登录用户信息，忽略这些参数。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StpInterfaceImpl implements StpInterface {

    private final SysRoleService roleService;
    private final SysAuthService sysAuthService;

    /**
     * 获取当前登录用户的权限标识列表
     *
     * @param loginId 账号id
     * @param loginType 账号类型
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        try {
            if (LoginType.ADMIN.getCode().equals(loginType)) {
                return sysAuthService.getAccessCodes((Long) loginId);
            }
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("获取用户权限列表异常, loginId={}, loginType={}", loginId, loginType, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取当前登录用户的角色编码列表
     *
     * @param loginId 账号id
     * @param loginType 账号类型
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        try {
            if (LoginType.ADMIN.getCode().equals(loginType)) {
                return roleService.getRoleCodesByUserId(Long.parseLong(loginId.toString()));
            }
            return Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
