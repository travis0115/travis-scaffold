package com.travis.monolith.app.user.internal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 客户端用户登录日志实体，对应 app_user_login_log 表。 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("app_user_login_log")
public class AppUserLoginLog implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String username;
    private String ip;
    private String location;
    private String browser;
    private String os;
    private Integer status;
    private String message;
    private LocalDateTime loginTime;
}
