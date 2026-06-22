package com.travis.monolith.system.menu.api.request;

import cn.hutool.core.util.StrUtil;
import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.monolith.system.common.api.enums.MenuType;
import jakarta.validation.constraints.*;
import java.util.Objects;
import lombok.Data;

/** 菜单修改请求参数 */
@Data
public class SysMenuUpdateReq {
    @NotNull(message = "父菜单ID不能为空")
    private Long parentId;

    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50, message = "菜单名称长度不能超过50个字符")
    private String menuName;

    @Size(max = 200, message = "路由路径长度不能超过200个字符")
    private String path;

    @Size(max = 200, message = "组件路径长度不能超过200个字符")
    private String component;

    @Size(max = 200, message = "权限标识长度不能超过200个字符")
    private String perms;

    @NotNull(message = "菜单类型不能为空")
    @EnumValue(value = MenuType.class, message = "菜单类型错误")
    private Integer menuType;

    private String icon;

    @NotNull(message = "排序号不能为空")
    @Min(value = 0, message = "排序号不能小于0")
    @Max(value = 9999, message = "排序号不能大于9999")
    private Integer sort;

    private String meta;

    @AssertTrue(message = "目录和菜单的路由路径不能为空")
    public boolean isPathValidForRouteMenu() {
        return (!Objects.equals(menuType, MenuType.DIRECTORY.getValue())
                        && !Objects.equals(menuType, MenuType.MENU.getValue()))
                || StrUtil.isNotBlank(path);
    }
}
