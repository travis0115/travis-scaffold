package com.travis.monolith.system.user.api.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

/**
 * 用户角色分配请求参数
 *
 * @author travis
 */
@Data
public class SysUserRoleReq {
    /** 用户ID */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** 待分配的角色ID列表 */
    private List<Long> roleIds;
}
