package com.travis.monolith.system.menu.api.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 菜单新增请求参数
 *
 * @author travis
 */
@Data
public class SysMenuCreateReq {
    /** 父菜单ID(0 表示顶级菜单) */
    @NotNull(message = "父菜单ID不能为空")
    private Long parentId;

    /** 菜单名称 */
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50, message = "菜单名称长度不能超过50个字符")
    private String menuName;

    /** 路由路径 */
    @Size(max = 200, message = "路由路径长度不能超过200个字符")
    private String path;

    /** 前端组件路径 */
    @Size(max = 200, message = "组件路径长度不能超过200个字符")
    private String component;

    /** 权限标识（如 system:user:add） */
    @Size(max = 100, message = "权限标识长度不能超过100个字符")
    private String perms;

    /** 菜单类型（0-目录 1-菜单 2-按钮） */
    @NotNull(message = "菜单类型不能为空")
    private Integer menuType;

    /** 图标 */
    private String icon;

    /** 排序号 */
    private Integer sort;

    /** 状态（0-禁用 1-启用） */
    private Integer status;

    /** 路由元信息JSON（Vben Admin RouteMeta 扩展字段） */
    private String meta;

    @AssertTrue(message = "目录和菜单的路由路径不能为空")
    public boolean isPathValidForRouteMenu() {
        return !Integer.valueOf(0).equals(menuType) && !Integer.valueOf(1).equals(menuType)
                || path != null && !path.isBlank();
    }
}
