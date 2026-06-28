package com.travis.monolith.system.user.api.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员用户详情视图，包含部门名称和角色关联信息
 *
 * @author travis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysUserResp {
    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 头像地址 */
    private String avatar;

    /** 头像文件ID */
    private Long avatarFileId;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String mobile;

    /** 所属部门ID */
    private Long deptId;

    /** 所属部门名称（关联查询） */
    private String deptName;

    /** 状态（0-禁用 1-启用） */
    private Integer status;

    /** 是否在线 */
    private Boolean online;

    /** 最近上线时间 */
    private LocalDateTime lastOnlineTime;

    /** 最近上线IP */
    private String lastOnlineIp;

    /** 最近上线地点（IP解析） */
    private String lastOnlineLocation;

    /** 最近下线时间 */
    private LocalDateTime lastOfflineTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 已分配的角色ID列表 */
    private List<Long> roleIds;

    /** 已分配的角色名称列表 */
    private List<String> roleNames;
}
