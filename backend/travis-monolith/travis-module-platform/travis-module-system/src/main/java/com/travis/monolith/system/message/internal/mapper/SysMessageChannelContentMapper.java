package com.travis.monolith.system.message.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.system.message.internal.entity.SysMessageChannelContent;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 消息渠道内容 Mapper。 */
@Mapper
public interface SysMessageChannelContentMapper extends BaseMapperX<SysMessageChannelContent> {
    @Delete("DELETE FROM sys_message_channel_content WHERE message_id = #{messageId}")
    int deleteByMessageId(@Param("messageId") Long messageId);
}
