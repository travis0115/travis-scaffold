package com.travis.monolith.system.menu.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 菜单树形结构视图，用于后台管理界面的菜单管理
 *
 * @author travis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SysMenuResp {
    /** 菜单ID */
    private Long id;

    /** 父菜单ID */
    private Long parentId;

    /** 菜单名称 */
    private String menuName;

    /** 路由路径 */
    private String path;

    /** 前端组件路径 */
    private String component;

    /** 权限标识 */
    private String perms;

    /** 菜单类型（0-目录 1-菜单 2-按钮） */
    private Integer menuType;

    /** 图标 */
    private String icon;

    /** 排序号 */
    private Integer sort;

    /** 状态（0-禁用 1-启用） */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 路由元信息JSON */
    private String meta;

    /** 子菜单列表 */
    private List<SysMenuResp> children;
}
