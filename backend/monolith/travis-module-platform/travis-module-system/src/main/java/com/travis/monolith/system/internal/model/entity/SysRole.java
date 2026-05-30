package com.travis.monolith.system.internal.model.entity;

import com.travis.infrastructure.framework.mybatis.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体，对应 sys_role 表
 *
 * @author travis
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysRole extends BaseEntity {
    /**
     * 角色名称
     */
    private String roleName;
    /**
     * 角色编码（唯一标识，如 admin、editor）
     */
    private String roleCode;
    /**
     * 备注
     */
    private String remark;
    /**
     * 是否可修改（0-否 1-是）
     */
    private Integer modifiable;
    /**
     * 状态（0-禁用 1-启用）
     */
    private Integer status;
}
