package com.travis.monolith.system.internal.model.response.user;

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
public class UserInfoResp {
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

    /** 角色编码列表 */
    private List<String> roles;

    /** 角色名称列表 */
    private List<String> roleNames;

    /** 权限标识列表 */
    private List<String> permissions;

    /** 登录后默认跳转路径 */
    private String homePath;
}
