package com.travis.monolith.system.role.api.request;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.web.model.PageRequest;
import com.travis.monolith.system.common.api.enums.Status;
import jakarta.validation.constraints.Size;
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
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    private String roleName;

    /** 角色编码（模糊匹配） */
    @Size(max = 50, message = "角色编码长度不能超过50个字符")
    private String roleCode;

    /** 状态（0-禁用 1-启用） */
    @EnumValue(value = Status.class, message = "状态值错误")
    private Integer status;
}
