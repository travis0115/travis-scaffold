package com.travis.monolith.system.dict.internal.entity;

import com.travis.infrastructure.framework.mybatis.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类型实体，对应 sys_dict 表
 *
 * @author travis
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysDict extends BaseEntity {
    /** 字典名称（如：性别、状态） */
    private String dictName;

    /** 字典编码（如：gender、status） */
    private String dictCode;

    /** 状态（0-禁用 1-启用） */
    private Integer status;

    /** 备注 */
    private String remark;
}
