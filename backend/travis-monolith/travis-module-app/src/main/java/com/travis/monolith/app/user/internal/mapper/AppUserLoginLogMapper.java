package com.travis.monolith.app.user.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.app.user.internal.entity.AppUserLoginLog;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AppUserLoginLogMapper extends BaseMapperX<AppUserLoginLog> {

    /** 查询时间范围内成功登录的去重用户数。 */
    long selectSuccessfulUserCount(
            @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
