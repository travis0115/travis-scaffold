package com.travis.monolith.system.dict.internal.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.travis.infrastructure.framework.mybatis.core.BaseEntity;
import com.travis.monolith.system.dict.api.model.SysDictItemResp;
import java.util.List;
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

    /** 字典类型编码（如：gender、status） */
    private String dictType;

    /** 状态（0-禁用 1-启用） */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 子节点（字典数据项列表），仅用于树形接口返回，非数据库字段 */
    @TableField(exist = false)
    private List<SysDictItemResp> children;
}
