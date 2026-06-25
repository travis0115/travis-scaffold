package com.travis.monolith.system.message.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.message.api.response.SysUserMessagePageResp;
import com.travis.monolith.system.message.api.response.SysUserMessageRecentResp;
import com.travis.monolith.system.message.internal.entity.SysMessage;
import com.travis.monolith.system.message.internal.entity.SysMessageReceiver;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** 消息接收对象转换器。 */
@Mapper(config = BaseMapperConfig.class)
public interface SysMessageReceiverConverter {

    @Mapping(target = "id", source = "receiver.id")
    @Mapping(target = "messageId", source = "message.id")
    @Mapping(target = "title", source = "message.title")
    @Mapping(target = "content", source = "message.content")
    @Mapping(target = "messageType", source = "message.messageType")
    @Mapping(target = "readStatus", source = "receiver.readStatus")
    @Mapping(target = "readTime", source = "receiver.readTime")
    @Mapping(target = "publishTime", source = "message.publishTime")
    @Mapping(target = "createTime", source = "receiver.createTime")
    SysUserMessagePageResp toPageResp(SysMessageReceiver receiver, SysMessage message);

    @Mapping(target = "id", source = "receiver.id")
    @Mapping(target = "messageId", source = "message.id")
    @Mapping(target = "title", source = "message.title")
    @Mapping(target = "content", source = "message.content")
    @Mapping(target = "messageType", source = "message.messageType")
    @Mapping(target = "readStatus", source = "receiver.readStatus")
    @Mapping(target = "readTime", source = "receiver.readTime")
    @Mapping(target = "publishTime", source = "message.publishTime")
    @Mapping(target = "createTime", source = "receiver.createTime")
    SysUserMessageRecentResp toRecentResp(SysMessageReceiver receiver, SysMessage message);
}
