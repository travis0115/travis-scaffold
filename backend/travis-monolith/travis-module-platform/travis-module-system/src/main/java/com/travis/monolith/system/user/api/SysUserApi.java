package com.travis.monolith.system.user.api;

import com.travis.monolith.system.user.api.response.SysUserOptionResp;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/** 用户模块对外查询 API。 */
public interface SysUserApi {

    /** 查询所有用户ID。 */
    List<Long> listUserIds();

    /** 根据部门ID查询所有可用的用户ID。 */
    List<Long> listUserIdsByDeptIds(Collection<Long> deptIds);

    /** 根据用户ID查询用户名，不存在则返回 null */
    String getUsernameById(Long userId);

    /** 根据用户ID查询用户名，不存在则返回 null */
    Map<Long, String> getUsernameMapByIds(Collection<Long> userIds);

    /** 查询当前登录用户部门范围内的启用用户。 */
    List<SysUserOptionResp> listCurrentUserScopedOptions(String keyword, int limit);

    /** 根据ID查询当前登录用户部门范围内的启用用户。 */
    List<SysUserOptionResp> listCurrentUserScopedOptionsByIds(Collection<Long> userIds);
}
