package com.travis.monolith.system.internal.model.entity;

import com.travis.infrastructure.framework.mybatis.core.entity.BaseEntity;
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
    /**
     * 字典名称（如：性别、状态）
     */
    private String dictName;
    /**
     * 字典类型编码（如：gender、status）
     */
    private String dictType;
    /**
     * 状态（0-禁用 1-启用）
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;
}
