package com.travis.monolith.system.internal.model.request.dept;

import com.travis.infrastructure.framework.web.core.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门分页查询请求参数
 *
 * @author travis
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysDeptPageReq extends PageRequest {
    /**
     * 部门名称（模糊匹配）
     */
    private String deptName;
    /**
     * 状态（0-禁用 1-启用）
     */
    private Integer status;
}
