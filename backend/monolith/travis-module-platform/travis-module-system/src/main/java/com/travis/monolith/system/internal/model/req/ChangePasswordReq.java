package com.travis.monolith.system.internal.model.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改密码请求参数
 *
 * @author travis
 */
@Data
public class ChangePasswordReq {
    /** 旧密码 */
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;
    /** 新密码 */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度必须在6-20位之间")
    private String newPassword;
}
