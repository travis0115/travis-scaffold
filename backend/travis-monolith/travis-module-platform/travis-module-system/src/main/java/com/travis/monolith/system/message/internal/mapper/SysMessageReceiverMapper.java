package com.travis.monolith.system.message.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.system.message.internal.entity.SysMessageReceiver;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 消息接收记录 Mapper。 */
@Mapper
public interface SysMessageReceiverMapper extends BaseMapperX<SysMessageReceiver> {
    /** 物理删除消息的接收状态，避免保留无主记录。 */
    @Delete("DELETE FROM sys_message_receiver WHERE message_id = #{messageId}")
    int deleteByMessageId(@Param("messageId") Long messageId);
}
