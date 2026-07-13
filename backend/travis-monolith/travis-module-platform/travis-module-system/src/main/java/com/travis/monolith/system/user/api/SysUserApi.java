package com.travis.monolith.system.user.api;

import com.travis.monolith.system.user.api.response.SysUserOptionResp;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.validation.annotation.Validated;

/** 用户模块对外查询 API。 */
@Validated
public interface SysUserApi {

    /** 查询所有用户ID。 */
    List<Long> listUserIds();

    /** 根据部门ID查询所有可用的用户ID。 */
    List<Long> listUserIdsByDeptIds(Collection<@NotNull(message = "部门ID不能为空") Long> deptIds);

    /** 根据用户ID查询部门ID，不存在则返回 null。 */
    Long getDeptIdByUserId(Long userId);

    /** 根据用户ID查询用户名，不存在则返回 null */
    String getUsernameById(Long userId);

    /** 根据用户ID查询用户名，不存在则返回 null */
    Map<Long, String> getUsernameMapByIds(Collection<@NotNull(message = "用户ID不能为空") Long> userIds);

    /** 查询当前登录用户部门范围内的启用用户。 */
    List<SysUserOptionResp> listCurrentUserScopedOptions(
            String keyword,
            @Min(value = 1, message = "查询数量不能小于1") @Max(value = 50, message = "查询数量不能大于50")
                    int limit);

    /** 根据ID查询当前登录用户部门范围内的启用用户。 */
    List<SysUserOptionResp> listCurrentUserScopedOptionsByIds(
            Collection<@NotNull(message = "用户ID不能为空") Long> userIds);
}
