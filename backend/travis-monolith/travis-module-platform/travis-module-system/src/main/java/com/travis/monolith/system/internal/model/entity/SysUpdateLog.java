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
 * 系统更新日志实体，对应 sys_update_log 表，记录系统版本更新内容
 *
 * @author travis
 */
@Data
public class SysUpdateLog implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    /** 主键ID（雪花算法生成） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 版本号（如 v1.0） */
    private String version;

    /** 更新标题 */
    private String title;

    /** 更新内容（支持多行文本） */
    private String content;

    /** 发布时间 */
    private LocalDateTime publishTime;

    /** 状态（0-草稿 1-已发布） */
    private Integer status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 创建人ID */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 更新人ID */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;
}
