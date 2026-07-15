package com.travis.monolith.system.menu.api.request;

import cn.hutool.core.util.StrUtil;
import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.validation.annotation.JsonValue;
import com.travis.monolith.system.menu.api.enums.MenuType;
import jakarta.validation.constraints.*;
import java.util.Objects;
import lombok.Data;

/** 菜单修改请求参数 */
@Data
public class SysMenuUpdateReq {
    /** 父菜单 ID。 */
    @NotNull(message = "父菜单ID不能为空")
    private Long parentId;

    /** 菜单名称。 */
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50, message = "菜单名称长度不能超过50个字符")
    private String menuName;

    /** 前端路由路径。 */
    @Size(max = 255, message = "路由路径长度不能超过255个字符")
    private String path;

    /** 前端组件路径。 */
    @Size(max = 255, message = "组件路径长度不能超过255个字符")
    private String component;

    /** 权限标识。 */
    @Size(max = 255, message = "权限标识长度不能超过255个字符")
    private String perms;

    /** 菜单类型。 */
    @NotNull(message = "菜单类型不能为空")
    @EnumValue(value = MenuType.class, message = "菜单类型错误")
    private Integer menuType;

    /** 菜单图标。 */
    @Size(max = 100, message = "图标长度不能超过100个字符")
    private String icon;

    /** 排序号。 */
    @NotNull(message = "排序号不能为空")
    @Min(value = 0, message = "排序号不能小于0")
    @Max(value = 9999, message = "排序号不能大于9999")
    private Integer sort;

    /** 前端菜单扩展配置，使用 JSON 对象格式。 */
    @Size(max = 5000, message = "扩展配置不能超过5000字符")
    @JsonValue(message = "扩展配置必须是合法JSON对象")
    private String meta;

    /** 校验目录和菜单类型是否配置路由路径。 */
    @AssertTrue(message = "目录和菜单的路由路径不能为空")
    public boolean isPathValidForRouteMenu() {
        return (!Objects.equals(menuType, MenuType.DIRECTORY.getValue())
                        && !Objects.equals(menuType, MenuType.MENU.getValue()))
                || StrUtil.isNotBlank(path);
    }
}
