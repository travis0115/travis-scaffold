package com.travis.monolith.system.dept.api.request;

import com.travis.infrastructure.common.validation.annotation.In;
import com.travis.infrastructure.common.web.model.PageRequest;
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
    /** 部门名称（模糊匹配） */
    private String deptName;

    /** 状态（0-禁用 1-启用） */
    @In(
            value = {0, 1},
            message = "状态仅允许值为0或1")
    private Integer status;
}
