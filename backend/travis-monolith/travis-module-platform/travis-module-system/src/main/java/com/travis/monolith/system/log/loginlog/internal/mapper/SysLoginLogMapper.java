package com.travis.monolith.system.log.loginlog.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.system.log.loginlog.internal.entity.SysLoginLog;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 登录日志 Mapper 接口，继承 BaseMapperX 提供基础 CRUD 能力
 *
 * @author travis
 */
@Mapper
public interface SysLoginLogMapper extends BaseMapperX<SysLoginLog> {

    /** 查询时间范围内成功登录的去重用户数。 */
    long selectSuccessfulUserCount(
            @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
