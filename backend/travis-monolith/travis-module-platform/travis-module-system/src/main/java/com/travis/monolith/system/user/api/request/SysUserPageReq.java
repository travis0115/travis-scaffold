package com.travis.monolith.system.user.api.request;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.web.model.PageRequest;
import com.travis.monolith.system.common.api.enums.Status;
import jakarta.validation.constraints.Size;
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
    @Size(max = 16, message = "用户名长度不能超过16个字符")
    private String username;

    /** 昵称（模糊匹配） */
    @Size(max = 20, message = "昵称长度不能超过20个字符")
    private String nickname;

    /** 手机号（模糊匹配） */
    @Size(max = 11, message = "手机号长度不能超过11个字符")
    private String mobile;

    /** 邮箱（模糊匹配） */
    @Size(max = 50, message = "邮箱长度不能超过50个字符")
    private String email;

    /** 状态（0-禁用 1-启用） */
    @EnumValue(value = Status.class, message = "状态值错误")
    private Integer status;

    /** 所属部门ID */
    private Long deptId;

    /** 是否只查询在线用户 */
    private Boolean onlineOnly;
}
