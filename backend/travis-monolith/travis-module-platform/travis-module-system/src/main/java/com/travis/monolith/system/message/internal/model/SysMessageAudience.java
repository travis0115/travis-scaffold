package com.travis.monolith.system.message.internal.model;

import com.travis.infrastructure.framework.jackson.core.JsonUtil;
import com.travis.monolith.system.message.internal.entity.SysMessage;
import java.util.List;

/** 消息接收范围快照。 */
public record SysMessageAudience(
        String receiverType, Integer receiverScope, List<Long> receiverValues) {

    public static SysMessageAudience from(SysMessage message) {
        List<Long> receiverValues =
                message.getReceiverValues() == null || message.getReceiverValues().isBlank()
                        ? List.of()
                        : JsonUtil.parseArray(message.getReceiverValues(), Long.class);
        return new SysMessageAudience(
                message.getReceiverType(), message.getReceiverScope(), receiverValues);
    }
}
