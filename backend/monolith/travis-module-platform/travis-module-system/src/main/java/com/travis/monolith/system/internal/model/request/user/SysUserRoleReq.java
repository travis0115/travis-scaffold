package com.travis.monolith.system.internal.model.request.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

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
