package com.travis.monolith.system.user.api.request;

import com.travis.infrastructure.common.validation.annotation.Mobile;
import com.travis.infrastructure.common.validation.annotation.Username;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @Username(message = "用户名格式不正确，需以字母开头，仅支持字母、数字和下划线")
    @Size(min = 6, max = 16, message = "用户名长度为6-16个字符")
    private String username;

    /** 昵称 */
    @NotBlank(message = "昵称不能为空")
    @Size(min = 2, max = 20, message = "昵称长度为2-20个字符")
    private String nickname;

    /** 邮箱 */
    @Email private String email;

    /** 手机号 */
    @Mobile private String mobile;

    /** 所属部门ID */
    private Long deptId;

    /** 乐观锁版本号 */
    @NotNull(message = "版本号不能为空")
    private Integer version;
}
