package com.travis.monolith.system.internal.model.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    /** 真实姓名（取自昵称字段） */
    private String realName;
    /** 头像地址 */
    private String avatar;
    /** 角色编码列表 */
    private List<String> roles;
    /** 权限标识列表 */
    private List<String> permissions;
    /** 登录后默认跳转路径 */
    private String homePath;
}
