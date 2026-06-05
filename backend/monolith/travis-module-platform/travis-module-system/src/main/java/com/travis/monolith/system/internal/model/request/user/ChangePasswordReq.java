package com.travis.monolith.system.internal.model.request.user;

import com.travis.infrastructure.common.validation.annotation.Password;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改密码请求参数
 *
 * @author travis
 */
@Data
public class ChangePasswordReq {
    /**
     * 旧密码
     */
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;
    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    @Password(message = "密码需为8-32位，并包含大写字母、小写字母、数字、特殊符号中的至少3种")
    private String newPassword;
}
