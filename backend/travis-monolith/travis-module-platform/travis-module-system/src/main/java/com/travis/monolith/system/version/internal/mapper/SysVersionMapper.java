package com.travis.monolith.system.version.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.system.version.internal.entity.SysVersion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统版本日志 Mapper
 *
 * @author travis
 */
@Mapper
public interface SysVersionMapper extends BaseMapperX<SysVersion> {}
