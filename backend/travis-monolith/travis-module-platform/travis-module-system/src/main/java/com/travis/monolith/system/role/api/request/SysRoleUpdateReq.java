package com.travis.monolith.system.role.api.request;

import com.travis.monolith.system.common.api.constant.ValidationPattern;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 角色修改请求参数 */
@Data
public class SysRoleUpdateReq {
    /** 角色名称。 */
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    private String roleName;

    /** 角色编码。 */
    @NotBlank(message = "角色编码不能为空")
    @Pattern(regexp = ValidationPattern.CODE, message = "角色编码必须以字母开头，只能包含字母、数字和下划线")
    @Size(max = 50, message = "角色编码长度不能超过50个字符")
    private String roleCode;

    /** 备注。 */
    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;
}
