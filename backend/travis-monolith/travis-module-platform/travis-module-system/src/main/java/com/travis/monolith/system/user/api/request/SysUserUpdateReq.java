package com.travis.monolith.system.user.api.request;

import com.travis.infrastructure.common.validation.annotation.Mobile;
import com.travis.infrastructure.common.validation.annotation.Username;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员用户修改修改请求参数
 *
 * @author travis
 */
@Data
public class SysUserUpdateReq {
    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    @Username(message = "用户名格式不正确，需以字母开头，长度6-16位，仅支持字母、数字和下划线")
    private String username;

    /** 密码（明文，服务端使用 BCrypt 加密后存储） */
    private String password;

    /** 昵称 */
    @NotBlank(message = "昵称不能为空")
    @Size(min = 2, max = 20, message = "昵称长度为2-20个字符")
    private String nickname;

    /** 头像地址 */
    private String avatar;

    /** 邮箱 */
    @Email private String email;

    /** 手机号 */
    @Mobile private String mobile;

    /** 所属部门ID */
    private Long deptId;

    /** 状态（0-禁用 1-启用） */
    private Integer status;
}
