package com.travis.monolith.ops.errorlog.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.ops.errorlog.internal.entity.SysErrorLogOccurrence;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysErrorLogOccurrenceMapper extends BaseMapperX<SysErrorLogOccurrence> {}
