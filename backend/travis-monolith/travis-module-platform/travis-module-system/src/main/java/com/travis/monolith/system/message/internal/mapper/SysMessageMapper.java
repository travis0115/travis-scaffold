package com.travis.monolith.system.message.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.system.message.internal.entity.SysMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysMessageMapper extends BaseMapperX<SysMessage> {}
