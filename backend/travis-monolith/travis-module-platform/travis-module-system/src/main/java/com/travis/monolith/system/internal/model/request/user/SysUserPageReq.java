package com.travis.monolith.system.internal.model.request.user;

import com.travis.infrastructure.common.web.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户分页查询请求参数
 *
 * @author travis
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysUserPageReq extends PageRequest {
    /** 用户名（模糊匹配） */
    private String username;

    /** 手机号（模糊匹配） */
    private String phone;

    /** 状态（0-禁用 1-启用） */
    private Integer status;

    /** 所属部门ID */
    private Long deptId;
}
