package com.travis.monolith.system.user.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 用户选择项。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysUserOptionResp {
    /** 用户 ID。 */
    private Long id;

    /** 用户名。 */
    private String username;

    /** 昵称。 */
    private String nickname;

    /** 所属部门名称。 */
    private String deptName;
}
