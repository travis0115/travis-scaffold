package com.travis.monolith.system.menu.internal.api;

import com.travis.monolith.system.menu.api.SysMenuApi;
import com.travis.monolith.system.menu.api.response.VbenMenuResp;
import com.travis.monolith.system.menu.internal.service.SysMenuService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
    public List<VbenMenuResp> getVbenMenuTree(List<Long> roleIds) {
        return menuService.getVbenMenuTree(roleIds);
    }

    @Override
    public List<String> getPermissionsByMenuIds(List<Long> menuIds) {
        return menuService.getPermissionsByMenuIds(menuIds);
    }
}
