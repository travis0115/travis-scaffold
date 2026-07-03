package com.travis.monolith.system.user.api.request;

import com.travis.infrastructure.common.validation.annotation.Password;
import com.travis.infrastructure.framework.desensitize.core.annotation.slider.PasswordDesensitize;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改密码请求参数
 *
 * @author travis
 */
@Data
public class SysUserChangePasswordReq {
    /** 原密码 */
    @NotBlank(message = "原密码不能为空")
    @PasswordDesensitize
    private String oldPassword;

    /** 新密码 */
    @NotBlank(message = "新密码不能为空")
    @Password
    @PasswordDesensitize
    private String newPassword;
}
