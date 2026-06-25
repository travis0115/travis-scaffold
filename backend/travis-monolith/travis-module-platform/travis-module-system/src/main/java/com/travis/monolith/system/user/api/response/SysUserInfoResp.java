package com.travis.monolith.system.user.api.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前登录用户信息视图，用于前端展示用户资料和权限信息
 *
 * @author travis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysUserInfoResp {
    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 头像地址 */
    private String avatar;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String mobile;

    /** 所属部门ID */
    private Long deptId;

    /** 所属部门名称（关联查询） */
    private String deptName;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    /** 最后登录IP */
    private String lastLoginIp;

    /** 最后登录地点（IP解析） */
    private String lastLoginLocation;

    /** 角色编码列表 */
    private List<String> roles;

    /** 角色名称列表 */
    private List<String> roleNames;

    /** 权限标识列表 */
    private List<String> permissions;
}
