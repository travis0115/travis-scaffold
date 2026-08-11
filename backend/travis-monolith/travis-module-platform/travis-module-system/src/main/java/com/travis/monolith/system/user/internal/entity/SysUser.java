package com.travis.monolith.system.user.internal.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.Version;
import com.travis.infrastructure.framework.mybatis.core.BaseEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

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

    /** 头像文件ID */
    private Long avatarFileId;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String mobile;

    /** 所属部门ID */
    private Long deptId;

    /** 状态（0-禁用 1-启用） */
    private Integer status;

    /** 最近上线时间 */
    private LocalDateTime lastOnlineTime;

    /** 最近上线IP */
    private String lastOnlineIp;

    /** 最近上线地点 */
    private String lastOnlineLocation;

    /** 最近下线时间 */
    private LocalDateTime lastOfflineTime;

    /** 乐观锁版本号 */
    @Version private Integer version;
}
