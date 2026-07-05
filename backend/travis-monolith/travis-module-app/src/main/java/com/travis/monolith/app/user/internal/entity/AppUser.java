package com.travis.monolith.app.user.internal.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.travis.infrastructure.framework.mybatis.core.BaseEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AppUser extends BaseEntity {
    private String username;

    @TableField(select = false)
    private String password;

    private String nickname;
    private Long avatarFileId;
    private String email;
    private String mobile;
    private Integer status;
    private LocalDateTime lastOnlineTime;
    private String lastOnlineIp;
    private LocalDateTime lastOfflineTime;
    private Integer version;
}
