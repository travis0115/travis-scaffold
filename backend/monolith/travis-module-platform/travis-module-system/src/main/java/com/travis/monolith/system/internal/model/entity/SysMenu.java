package com.travis.monolith.system.internal.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 菜单实体，对应 sys_menu 表，支持树形结构
 *
 * @author travis
 */
@Data
public class SysMenu {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 上级菜单ID（0 表示顶级菜单）
     */
    private Long parentId;
    /**
     * 菜单名称
     */
    private String menuName;
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
     * 菜单类型（0-目录 1-菜单 2-按钮）
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
    /**
     * 路由元信息JSON（Vben Admin RouteMeta 的扩展字段，如 keepAlive、hideInMenu 等）
     */
    private String meta;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 创建人ID
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    /**
     * 更新人ID
     */
    @TableField(fill = FieldFill.UPDATE)
    private Long updateBy;
}