package com.travis.monolith.system.dept.api.event;

import java.util.List;

/**
 * 部门删除事件载荷，通知其他模块清理与被删除部门相关的数据
 *
 * @author travis
 * @param deptIds 被删除的部门ID列表（包含所有下级部门）
 */
public record DeptDeletedPayload(List<Long> deptIds) {}
