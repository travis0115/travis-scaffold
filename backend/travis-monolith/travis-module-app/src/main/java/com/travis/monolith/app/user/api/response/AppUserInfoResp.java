package com.travis.monolith.app.user.api.response;

import lombok.Data;

@Data
public class AppUserInfoResp {
    private Long id;
    private String username;
    private String nickname;
    private Long avatarFileId;
    private String email;
    private String mobile;
}
