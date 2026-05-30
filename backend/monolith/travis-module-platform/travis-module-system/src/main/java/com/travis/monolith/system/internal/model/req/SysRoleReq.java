package com.travis.monolith.system.internal.model.req;

import jakarta.validation.constraints.NotBlank;
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
    private String roleName;
    /** 角色编码（唯一标识） */
    @NotBlank(message = "角色编码不能为空")
    private String roleCode;
    /** 备注 */
    private String remark;
    /** 是否可编辑（0-否 1-是） */
    private Integer modifiable;
    /** 状态（0-禁用 1-启用） */
    private Integer status;
}
