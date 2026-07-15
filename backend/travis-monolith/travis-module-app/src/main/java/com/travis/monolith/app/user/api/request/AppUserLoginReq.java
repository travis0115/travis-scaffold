package com.travis.monolith.app.user.api.request;

import com.travis.infrastructure.common.validation.annotation.Password;
import com.travis.infrastructure.common.validation.annotation.Username;
import com.travis.infrastructure.framework.desensitize.core.annotation.slider.PasswordDesensitize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 客户端用户登录参数。 */
@Data
public class AppUserLoginReq {
    /** 登录用户名。 */
    @NotBlank(message = "用户名不能为空")
    @Username(message = "用户名格式不正确，需以字母开头，仅支持字母、数字和下划线")
    @Size(min = 6, max = 16, message = "用户名长度为6-16个字符")
    private String username;

    /** 登录密码。 */
    @NotBlank(message = "密码不能为空")
    @Password
    @PasswordDesensitize
    private String password;
}
