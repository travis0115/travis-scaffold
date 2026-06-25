package com.travis.monolith.system.message.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.system.message.internal.entity.SysMessageReceiver;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysMessageReceiverMapper extends BaseMapperX<SysMessageReceiver> {
    @Delete(
            """
            DELETE FROM sys_message_receiver
            WHERE id = #{id} AND receiver_type = #{receiverType} AND receiver_id = #{receiverId}
            """)
    int deleteMessage(
            @Param("id") Long id,
            @Param("receiverType") String receiverType,
            @Param("receiverId") Long receiverId);

    @Delete(
            """
            DELETE FROM sys_message_receiver
            WHERE receiver_type = #{receiverType} AND receiver_id = #{receiverId}
            """)
    int deleteByReceiver(
            @Param("receiverType") String receiverType, @Param("receiverId") Long receiverId);

    @Delete("DELETE FROM sys_message_receiver WHERE message_id = #{messageId}")
    int deleteByMessageId(@Param("messageId") Long messageId);
}
