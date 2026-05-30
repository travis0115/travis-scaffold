package com.travis.monolith.system.internal.model.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理员用户新增/编辑请求参数
 *
 * @author travis
 */
@Data
public class SysUserReq {
    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    private String username;
    /** 密码（明文，服务端使用 BCrypt 加密后存储） */
    private String password;
    /** 昵称 */
    @NotBlank(message = "昵称不能为空")
    private String nickname;
    /** 头像地址 */
    private String avatar;
    /** 邮箱 */
    private String email;
    /** 手机号 */
    private String phone;
    /** 所属部门ID */
    private Long deptId;
    /** 状态（0-禁用 1-启用） */
    private Integer status;
}
