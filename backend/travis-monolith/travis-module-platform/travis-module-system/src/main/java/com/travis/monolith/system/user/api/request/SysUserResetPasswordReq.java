package com.travis.monolith.system.user.api.request;

import com.travis.infrastructure.common.validation.annotation.Password;
import lombok.Data;

/**
 * 管理员重置用户密码请求参数
 *
 * @author travis
 */
@Data
public class SysUserResetPasswordReq {
    /** 新密码（可选，不传则使用默认密码） */
    @Password
    private String newPassword;
}
