package com.travis.monolith.system.user.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新头像请求参数
 *
 * @author travis
 */
@Data
public class SysUserUpdateAvatarReq {
    /** 头像文件ID */
    @NotNull(message = "头像文件ID不能为空")
    private Long avatarFileId;
}
