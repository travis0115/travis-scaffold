package com.travis.monolith.system.internal.model.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新头像请求参数
 *
 * @author travis
 */
@Data
public class UpdateAvatarReq {
    /** 头像地址 */
    @NotBlank(message = "头像地址不能为空")
    private String avatar;
}
