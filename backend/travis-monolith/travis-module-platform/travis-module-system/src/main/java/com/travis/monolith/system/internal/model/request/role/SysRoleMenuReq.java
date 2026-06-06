package com.travis.monolith.system.internal.model.request.role;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

/**
 * 角色菜单分配请求参数
 *
 * @author travis
 */
@Data
public class SysRoleMenuReq {
    /** 角色ID */
    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    /** 待分配的菜单ID列表 */
    private List<Long> menuIds;
}
