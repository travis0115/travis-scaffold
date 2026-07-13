package com.travis.monolith.system.menu.api;

import com.travis.monolith.system.menu.api.response.VbenMenuResp;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.validation.annotation.Validated;

/**
 * 菜单模块对外 API，供跨模块调用，只暴露 DTO，不暴露 entity
 *
 * @author travis
 */
@Validated
public interface SysMenuApi {

    /**
     * 根据用户ID生成 Vben Admin 格式的菜单树
     *
     * @param userId 用户ID
     * @return Vben Admin 格式的菜单树
     */
    List<VbenMenuResp> getVbenMenuTree(@NotNull(message = "用户ID不能为空") Long userId);

    /**
     * 根据菜单ID列表查询已启用菜单的权限标识列表
     *
     * @param menuIds 菜单ID列表
     * @return 权限标识列表（去重）
     */
    List<String> getPermissionsByMenuIds(List<@NotNull(message = "菜单ID不能为空") Long> menuIds);
}
