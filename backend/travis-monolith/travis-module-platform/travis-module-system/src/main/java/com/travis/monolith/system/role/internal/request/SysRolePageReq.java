package com.travis.monolith.system.role.internal.request;

import com.travis.infrastructure.common.web.model.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色分页查询请求参数
 *
 * @author travis
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysRolePageReq extends PageRequest {
    /** 角色名称（模糊匹配） */
    private String roleName;

    /** 角色编码（模糊匹配） */
    private String roleCode;

    /** 状态（0-禁用 1-启用） */
    private Integer status;
}
