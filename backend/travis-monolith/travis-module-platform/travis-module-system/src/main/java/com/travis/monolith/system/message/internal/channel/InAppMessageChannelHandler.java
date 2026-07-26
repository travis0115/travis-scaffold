package com.travis.monolith.system.message.internal.channel;

import com.travis.monolith.system.message.api.enums.SysMessageChannel;
import com.travis.monolith.system.message.internal.entity.SysMessage;
import org.springframework.stereotype.Component;

/** 站内信通道处理器。 */
@Component
public class InAppMessageChannelHandler implements SysMessageChannelHandler {

    @Override
    public String getChannel() {
        return SysMessageChannel.IN_APP.getValue();
    }

    @Override
    public void send(SysMessage message) {
        // 站内信保存消息记录后即完成投递，无需调用外部通道。
    }
}
