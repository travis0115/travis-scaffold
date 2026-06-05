package com.travis.monolith.system.internal.model.entity;

import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
import com.travis.infrastructure.framework.mybatis.core.entity.BaseEntity;
import com.travis.monolith.system.internal.model.response.dict.SysDictItemResp;
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

    /**
     * 子节点（字典数据项列表），仅用于树形接口返回，非数据库字段
     */
    @TableField(exist = false)
    private List<SysDictItemResp> children;
}
