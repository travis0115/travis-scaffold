package com.travis.monolith.system.user.api;

import java.util.Collection;
import java.util.List;

/** 用户模块对外查询 API。 */
public interface SysUserApi {
    List<Long> listEnabledUserIds();

    List<Long> listEnabledUserIdsByIds(Collection<Long> userIds);

    List<Long> listEnabledUserIdsByDeptIds(Collection<Long> deptIds);
}
