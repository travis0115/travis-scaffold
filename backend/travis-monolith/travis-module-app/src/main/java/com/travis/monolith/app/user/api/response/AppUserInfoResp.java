package com.travis.monolith.app.user.api.response;

import lombok.Data;

/** 当前客户端用户信息。 */
@Data
public class AppUserInfoResp {
    /** 用户 ID。 */
    private Long id;

    /** 用户名。 */
    private String username;

    /** 昵称。 */
    private String nickname;

    /** 头像文件 ID。 */
    private Long avatarFileId;

    /** 邮箱。 */
    private String email;

    /** 手机号。 */
    private String mobile;
}
