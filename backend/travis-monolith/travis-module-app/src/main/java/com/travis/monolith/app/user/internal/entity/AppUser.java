package com.travis.monolith.app.user.internal.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.Version;
import com.travis.infrastructure.framework.mybatis.core.BaseEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 客户端用户实体。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AppUser extends BaseEntity {
    /** 登录用户名。 */
    private String username;

    /** BCrypt 加密后的登录密码，默认查询不返回。 */
    @TableField(select = false)
    private String password;

    /** 昵称。 */
    private String nickname;

    /** 头像文件 ID。 */
    private Long avatarFileId;

    /** 邮箱。 */
    private String email;

    /** 手机号。 */
    private String mobile;

    /** 用户状态。 */
    private Integer status;

    /** 最近一次上线时间。 */
    private LocalDateTime lastOnlineTime;

    /** 最近一次上线 IP。 */
    private String lastOnlineIp;

    /** 最近一次离线时间。 */
    private LocalDateTime lastOfflineTime;

    /** 乐观锁版本号。 */
    @Version private Integer lockVersion;
}
