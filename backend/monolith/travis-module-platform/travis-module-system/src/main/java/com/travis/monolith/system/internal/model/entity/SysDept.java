package com.travis.monolith.system.internal.model.entity;

import com.travis.infrastructure.framework.mybatis.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门实体，对应 sys_dept 表，支持树形结构
 *
 * @author travis
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysDept extends BaseEntity {
    /**
     * 上级部门ID（0 表示顶级部门）
     */
    private Long parentId;
    /**
     * 部门名称
     */
    private String deptName;
    /**
     * 排序号（升序）
     */
    private Integer sort;
    /**
     * 负责人
     */
    private String leader;
    /**
     * 联系电话
     */
    private String mobile;
    /**
     * 状态（0-禁用 1-启用）
     */
    private Integer status;
}
