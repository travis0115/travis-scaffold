package com.travis.monolith.system.message.internal.channel;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** 消息通道处理器注册表。 */
@Component
public class SysMessageChannelHandlerRegistry {

    private final Map<String, SysMessageChannelHandler> handlers;

    public SysMessageChannelHandlerRegistry(List<SysMessageChannelHandler> handlers) {
        this.handlers =
                handlers.stream()
                        .collect(
                                Collectors.toUnmodifiableMap(
                                        SysMessageChannelHandler::getChannel, Function.identity()));
    }

    /** 获取指定通道的处理器。 */
    public SysMessageChannelHandler get(String channel) {
        return handlers.get(channel);
    }
}
