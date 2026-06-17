package com.travis.monolith.system.role.api.request;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.monolith.system.common.api.constant.ValidationPattern;
import com.travis.monolith.system.common.api.enums.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 角色新增请求参数
 *
 * @author travis
 */
@Data
public class SysRoleCreateReq {
    /** 角色名称 */
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    private String roleName;

    /** 角色编码（唯一标识） */
    @NotBlank(message = "角色编码不能为空")
    @Pattern(regexp = ValidationPattern.CODE, message = "角色编码必须以字母开头，只能包含字母、数字和下划线")
    @Size(max = 50, message = "角色编码长度不能超过50个字符")
    private String roleCode;

    /** 备注 */
    @Size(max = 200, message = "备注长度不能超过200个字符")
    private String remark;

    /** 状态（0-禁用 1-启用） */
    @EnumValue(value = Status.class, message = "状态值错误")
    @NotNull(message = "状态值不允许为空")
    private Integer status;
}
