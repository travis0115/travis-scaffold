package com.travis.monolith.system.internal.model.req;

import lombok.Data;

/**
 * 管理员重置用户密码请求参数
 *
 * @author travis
 */
@Data
public class ResetPasswordReq {
    /** 新密码（可选，不传则使用默认密码） */
    private String newPassword;
}
