package com.travis.monolith.system.internal.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户-角色关联实体，对应 sys_user_role 表，用于用户与角色的多对多关联
 *
 * @author travis
 */
@Data
public class SysUserRole implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    /** 主键ID（雪花算法生成） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 角色ID */
    private Long roleId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 创建人ID */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;
}
