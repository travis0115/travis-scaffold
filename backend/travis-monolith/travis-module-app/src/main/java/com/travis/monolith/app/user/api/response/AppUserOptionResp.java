package com.travis.monolith.app.user.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 客户端用户选择项。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppUserOptionResp {
    /** 用户 ID。 */
    private Long id;

    /** 用户名。 */
    private String username;

    /** 昵称。 */
    private String nickname;

    /** 手机号。 */
    private String mobile;
}
