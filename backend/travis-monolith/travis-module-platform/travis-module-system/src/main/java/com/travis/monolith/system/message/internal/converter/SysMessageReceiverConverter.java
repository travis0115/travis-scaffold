package com.travis.monolith.system.message.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.message.api.response.SysUserMessageResp;
import com.travis.monolith.system.message.api.response.SysUserMessageRecentResp;
import com.travis.monolith.system.message.internal.entity.SysMessage;
import com.travis.monolith.system.message.internal.entity.SysMessageReceiver;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** 消息接收对象转换器。 */
@Mapper(config = BaseMapperConfig.class)
public interface SysMessageReceiverConverter {

    @Mapping(target = "id", source = "message.id")
    @Mapping(target = "messageId", source = "message.id")
    @Mapping(target = "title", source = "message.title")
    @Mapping(target = "content", source = "message.content")
    @Mapping(target = "messageType", source = "message.messageType")
    @Mapping(
            target = "readStatus",
            expression =
                    "java(receiver == null || receiver.getReadStatus() == null ? 0 : receiver.getReadStatus())")
    @Mapping(
            target = "readTime",
            expression = "java(receiver == null ? null : receiver.getReadTime())")
    @Mapping(target = "publishTime", source = "message.publishTime")
    @Mapping(target = "createTime", source = "message.createTime")
    SysUserMessageResp toPageResp(SysMessage message, SysMessageReceiver receiver);

    @Mapping(target = "id", source = "message.id")
    @Mapping(target = "messageId", source = "message.id")
    @Mapping(target = "title", source = "message.title")
    @Mapping(target = "content", source = "message.content")
    @Mapping(target = "messageType", source = "message.messageType")
    @Mapping(
            target = "readStatus",
            expression =
                    "java(receiver == null || receiver.getReadStatus() == null ? 0 : receiver.getReadStatus())")
    @Mapping(
            target = "readTime",
            expression = "java(receiver == null ? null : receiver.getReadTime())")
    @Mapping(target = "publishTime", source = "message.publishTime")
    @Mapping(target = "createTime", source = "message.createTime")
    SysUserMessageRecentResp toRecentResp(SysMessage message, SysMessageReceiver receiver);
}
