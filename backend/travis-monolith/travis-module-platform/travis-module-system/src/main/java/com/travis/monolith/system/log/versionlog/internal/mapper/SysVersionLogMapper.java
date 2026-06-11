package com.travis.monolith.system.log.versionlog.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.system.log.versionlog.internal.entity.SysVersionLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统版本日志 Mapper
 *
 * @author travis
 */
@Mapper
public interface SysVersionLogMapper extends BaseMapperX<SysVersionLog> {}
