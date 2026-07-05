package com.travis.monolith.app.user.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 客户端用户选择项。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppUserOptionResp {
    private Long id;
    private String username;
    private String nickname;
    private String mobile;
}
