package com.travis.monolith.system.user.api.model;

import jakarta.validation.constraints.NotBlank;
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
    private String username;

    /** 密码（明文，服务端校验后不存储） */
    @NotBlank(message = "密码不能为空")
    private String password;
}
