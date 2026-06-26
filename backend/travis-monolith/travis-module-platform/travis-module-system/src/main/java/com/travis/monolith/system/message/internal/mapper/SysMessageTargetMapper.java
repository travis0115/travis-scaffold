package com.travis.monolith.system.message.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.system.message.internal.entity.SysMessageTarget;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysMessageTargetMapper extends BaseMapperX<SysMessageTarget> {
    @Delete("DELETE FROM sys_message_target WHERE message_id = #{messageId}")
    int deleteByMessageId(@Param("messageId") Long messageId);
}
