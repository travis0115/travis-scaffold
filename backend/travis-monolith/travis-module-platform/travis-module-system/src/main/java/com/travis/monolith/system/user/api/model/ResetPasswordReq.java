package com.travis.monolith.system.user.api.model;

import com.travis.infrastructure.common.validation.annotation.Password;
import lombok.Data;

/**
 * 管理员重置用户密码请求参数
 *
 * @author travis
 */
@Data
public class ResetPasswordReq {
    /** 新密码（可选，不传则使用默认密码） */
    @Password(message = "密码需为8-32位，并包含大写字母、小写字母、数字、特殊符号中的至少3种")
    private String newPassword;
}
