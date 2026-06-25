package com.travis.monolith.system.user.api.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新头像请求参数
 *
 * @author travis
 */
@Data
public class SysUserUpdateAvatarReq {
    /** 头像地址 */
    @Size(max = 255, message = "头像地址长度不能超过255个字符")
    private String avatar;
}
