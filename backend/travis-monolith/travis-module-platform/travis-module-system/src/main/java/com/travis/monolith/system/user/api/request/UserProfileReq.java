package com.travis.monolith.system.user.api.request;

import com.travis.infrastructure.common.validation.annotation.Mobile;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 当前登录用户修改个人资料请求参数
 *
 * @author travis
 */
@Data
public class UserProfileReq {
    /** 昵称 */
    @NotBlank(message = "昵称不能为空")
    private String nickname;

    /** 邮箱 */
    @Email private String email;

    /** 手机号 */
    @Mobile private String mobile;
}
