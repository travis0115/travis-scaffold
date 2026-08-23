package com.travis.monolith.system.user.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.system.user.internal.entity.SysUser;
import com.travis.monolith.system.user.internal.model.SysUserCountSummary;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 管理员用户 Mapper 接口，继承 BaseMapperX 提供基础 CRUD 能力
 *
 * @author travis
 */
@Mapper
public interface SysUserMapper extends BaseMapperX<SysUser> {

    /** 查询用户总数及今日新增数。 */
    SysUserCountSummary selectCountSummary(@Param("todayStart") LocalDateTime todayStart);
}
