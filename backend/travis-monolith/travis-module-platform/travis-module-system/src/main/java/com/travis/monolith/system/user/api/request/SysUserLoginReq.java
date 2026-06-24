package com.travis.monolith.system.user.api.request;

import com.travis.infrastructure.common.validation.annotation.Password;
import com.travis.infrastructure.common.validation.annotation.Username;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登录请求参数
 *
 * @author travis
 */
@Data
public class SysUserLoginReq {
    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    @Username(message = "用户名格式不正确，需以字母开头，仅支持字母、数字和下划线")
    @Size(min = 6, max = 16, message = "用户名长度为6-16个字符")
    private String username;

    /** 密码（明文，服务端校验后不存储） */
    @NotBlank(message = "密码不能为空")
    @Password
    private String password;
}
