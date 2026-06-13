package com.travis.monolith.system.user.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 用户选择项。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysUserOptionResp {
    private Long id;
    private String username;
    private String nickname;
    private String deptName;
}
