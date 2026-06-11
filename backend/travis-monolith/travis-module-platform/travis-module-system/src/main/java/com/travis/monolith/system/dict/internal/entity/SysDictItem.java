package com.travis.monolith.system.dict.internal.entity;

import com.travis.infrastructure.framework.mybatis.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据项实体，对应 sys_dict_item 表
 *
 * @author travis
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysDictItem extends BaseEntity {
    /** 所属字典类型ID */
    private Long dictId;

    /** 字典项标签（显示文本） */
    private String label;

    /** 字典项值（实际存储值） */
    private String value;

    /** 展示样式（default/success/warning/danger/info/primary） */
    private String tagStyle;

    /** 排序号（升序） */
    private Integer sort;

    /** 状态（0-禁用 1-启用） */
    private Integer status;

    /** 备注 */
    private String remark;
}
