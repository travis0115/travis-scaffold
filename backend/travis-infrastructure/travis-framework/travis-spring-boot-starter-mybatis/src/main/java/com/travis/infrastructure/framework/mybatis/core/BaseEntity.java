package com.travis.infrastructure.framework.mybatis.core;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 数据库实体基类
 *
 * <p>提供通用字段：主键、创建/更新时间、创建/更新者、逻辑删除标记。 所有业务实体应继承此类以获得统一的审计字段与逻辑删除支持。
 *
 * @author travis
 */
@Data
public abstract class BaseEntity implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 创建人ID */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** 更新时间 */
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    /** 更新人ID */
    @TableField(fill = FieldFill.UPDATE)
    private Long updateBy;

    /** 逻辑删除标记（false=未删除, true=已删除） */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDeleted;
}
