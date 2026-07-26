package com.travis.monolith.system.message.internal.channel;

import com.travis.monolith.system.message.internal.entity.SysMessage;

/** 消息通道处理器。 */
public interface SysMessageChannelHandler {

    /** 返回处理器支持的消息通道。 */
    String getChannel();

    /** 发送消息。 */
    void send(SysMessage message);
}
