package com.travis.monolith.system.internal.model.response.role;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色详情视图，包含已分配的菜单ID列表
 *
 * @author travis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysRoleResp {
    /** 角色ID */
    private Long id;

    /** 角色名称 */
    private String roleName;

    /** 角色编码 */
    private String roleCode;

    /** 备注 */
    private String remark;

    /** 是否可编辑（0-否 1-是） */
    private Integer modifiable;

    /** 状态（0-禁用 1-启用） */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 已分配的菜单ID列表 */
    private List<Long> menuIds;
}
