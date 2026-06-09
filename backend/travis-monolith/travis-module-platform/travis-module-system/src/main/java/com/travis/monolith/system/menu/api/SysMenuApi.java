package com.travis.monolith.system.menu.api;

import com.travis.monolith.system.menu.api.response.VbenMenuResp;
import java.util.List;

/**
 * 菜单模块对外 API，供跨模块调用，只暴露 DTO，不暴露 entity
 *
 * @author travis
 */
public interface SysMenuApi {

    /**
     * 根据角色ID列表生成 Vben Admin 格式的菜单树
     *
     * @param roleIds 角色ID列表
     * @return Vben Admin 格式的菜单树
     */
    List<VbenMenuResp> getVbenMenuTree(List<Long> roleIds);

    /**
     * 根据菜单ID列表查询已启用菜单的权限标识列表
     *
     * @param menuIds 菜单ID列表
     * @return 权限标识列表（去重）
     */
    List<String> getPermissionsByMenuIds(List<Long> menuIds);
}
