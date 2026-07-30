package com.travis.monolith.system.message.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.system.message.internal.entity.SysMessageTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 消息模板 Mapper。 */
@Mapper
public interface SysMessageTemplateMapper extends BaseMapperX<SysMessageTemplate> {

    /** 物理删除消息模板，允许相同编码和通道后续重新创建。 */
    int deletePhysicallyById(@Param("id") Long id);
}
