package com.travis.monolith.system.user.internal.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.travis.infrastructure.framework.mybatis.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理员用户实体，对应 sys_user 表
 *
 * @author travis
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysUser extends BaseEntity {
    /** 用户名 */
    private String username;

    /** 密码（BCrypt 加密存储，标记 select=false 默认不查询） */
    @TableField(select = false)
    private String password;

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

    /** 邀请码 */
    private String invitationCode;

    /** 可用余额 */
    private BigDecimal availableBalance;

    /** 状态（0-禁用 1-启用） */
    private Integer status;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    /** 最后登录IP */
    private String lastLoginIp;

    /** 乐观锁版本号 */
    private Integer version;
}
