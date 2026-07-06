package com.travis.monolith.system.message.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.system.message.internal.entity.SysMessageTemplate;
import org.apache.ibatis.annotations.Mapper;

/** 消息模板 Mapper。 */
@Mapper
public interface SysMessageTemplateMapper extends BaseMapperX<SysMessageTemplate> {}
