package com.travis.monolith.system.user.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/** 用户模块对外查询 API。 */
public interface SysUserApi {
    List<Long> listEnabledUserIds();

    List<Long> listEnabledUserIdsByIds(Collection<Long> userIds);

    List<Long> listEnabledUserIdsByDeptIds(Collection<Long> deptIds);

    /** 根据用户ID查询用户名，不存在则返回 null */
    String getUsernameById(Long userId);

    Map<Long, String> getUsernameMapByIds(Collection<Long> userIds);
}
