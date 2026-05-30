package com.travis.monolith.system.internal.model.entity;

import com.travis.infrastructure.framework.mybatis.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单实体，对应 sys_menu 表，支持树形结构
 *
 * @author travis
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysMenu extends BaseEntity {
    /**
     * 上级菜单ID（0 表示顶级菜单）
     */
    private Long parentId;
    /**
     * 菜单名称
     */
    private String name;
    /**
     * 路由路径
     */
    private String path;
    /**
     * 前端组件路径
     */
    private String component;
    /**
     * 权限标识（如 system:user:add）
     */
    private String perms;
    /**
     * 菜单类型（0-目录/菜单 1-按钮）
     */
    private Integer menuType;
    /**
     * 图标
     */
    private String icon;
    /**
     * 排序号（升序）
     */
    private Integer sort;
    /**
     * 状态（0-禁用 1-启用）
     */
    private Integer status;
}
