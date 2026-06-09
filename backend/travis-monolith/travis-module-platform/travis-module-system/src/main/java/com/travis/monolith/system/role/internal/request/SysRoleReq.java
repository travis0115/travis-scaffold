package com.travis.monolith.system.role.internal.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 角色新增/编辑请求参数
 *
 * @author travis
 */
@Data
public class SysRoleReq {
    /** 角色名称 */
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    private String roleName;

    /** 角色编码（唯一标识） */
    @NotBlank(message = "角色编码不能为空")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]+$", message = "角色编码必须以字母开头，只能包含字母、数字和下划线")
    @Size(max = 50, message = "角色编码长度不能超过50个字符")
    private String roleCode;

    /** 备注 */
    @Size(max = 200, message = "备注长度不能超过200个字符")
    private String remark;

    /** 是否可编辑（0-否 1-是） */
    private Integer modifiable;

    /** 状态（0-禁用 1-启用） */
    private Integer status;
}
