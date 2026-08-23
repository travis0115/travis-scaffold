package com.travis.monolith.app.user.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.app.user.internal.entity.AppUser;
import com.travis.monolith.app.user.internal.model.AppUserCountSummary;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AppUserMapper extends BaseMapperX<AppUser> {

    /** 查询客户端用户总数及今日新增数。 */
    AppUserCountSummary selectCountSummary(@Param("todayStart") LocalDateTime todayStart);
}
