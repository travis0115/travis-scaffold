package com.travis.monolith.system.user.api;

import com.travis.monolith.system.user.api.response.SysUserOptionResp;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.validation.annotation.Validated;

/** 用户模块对外查询 API。 */
@Validated
public interface SysUserApi {

    /** 根据用户ID查询部门ID，不存在则返回 null。 */
    Long getDeptIdByUserId(Long userId);

    /** 根据用户ID查询用户名，不存在则返回 null */
    String getUsernameById(Long userId);

    /** 根据用户ID查询用户名，不存在则返回 null */
    Map<Long, String> getUsernameMapByIds(Collection<Long> userIds);

    /** 查询当前登录用户部门范围内的启用用户。 */
    List<SysUserOptionResp> listCurrentUserScopedOptions(String keyword, int limit);

    /** 根据ID查询当前登录用户部门范围内的启用用户。 */
    List<SysUserOptionResp> listCurrentUserScopedOptionsByIds(
            @NotNull(message = "用户ID不能为空") Collection<Long> userIds);
}
