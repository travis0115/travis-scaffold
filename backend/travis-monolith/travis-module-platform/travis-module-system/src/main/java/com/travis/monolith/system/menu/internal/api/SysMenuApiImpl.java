package com.travis.monolith.system.menu.internal.api;

import com.travis.monolith.system.menu.api.SysMenuApi;
import com.travis.monolith.system.menu.api.response.VbenMenuResp;
import com.travis.monolith.system.menu.internal.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 菜单模块对外 API 实现，委托调用内部 Service
 *
 * @author travis
 */
@Component
@RequiredArgsConstructor
public class SysMenuApiImpl implements SysMenuApi {

    private final SysMenuService menuService;

    @Override
    public List<VbenMenuResp> getVbenMenuTree(Long userId) {
        return menuService.getVbenMenuTree(userId);
    }

    @Override
    public List<String> getPermissionsByMenuIds(List<Long> menuIds) {
        return menuService.getPermissionsByMenuIds(menuIds);
    }
}
