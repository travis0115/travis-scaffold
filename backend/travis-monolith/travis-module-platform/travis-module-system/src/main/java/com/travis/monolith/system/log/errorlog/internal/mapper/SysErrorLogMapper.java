package com.travis.monolith.system.log.errorlog.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.system.log.errorlog.internal.entity.SysErrorLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysErrorLogMapper extends BaseMapperX<SysErrorLog> {}
