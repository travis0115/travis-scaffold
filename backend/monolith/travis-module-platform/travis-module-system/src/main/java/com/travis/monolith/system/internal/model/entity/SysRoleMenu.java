package com.travis.monolith.system.internal.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色-菜单关联实体，对应 sys_role_menu 表，用于 RBAC 权限模型中角色与菜单的多对多关联
 *
 * @author travis
 */
@Data
public class SysRoleMenu implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 角色ID
     */
    private Long roleId;
    /**
     * 菜单ID
     */
    private Long menuId;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 创建人ID
     */
    private Long createBy;
}
