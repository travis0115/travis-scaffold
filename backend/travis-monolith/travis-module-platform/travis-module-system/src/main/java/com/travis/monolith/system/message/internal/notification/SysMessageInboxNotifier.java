package com.travis.monolith.system.message.internal.notification;

import com.travis.infrastructure.framework.satoken.core.websocket.SaTokenWebSocketPrincipal;
import com.travis.infrastructure.framework.websocket.core.message.WebSocketMessage;
import com.travis.infrastructure.framework.websocket.core.message.WebSocketSender;
import com.travis.infrastructure.framework.websocket.core.sender.WebSocketMessageSender;
import com.travis.monolith.system.message.api.enums.SysMessageChannel;
import com.travis.monolith.system.message.api.enums.SysMessageWebSocketEvent;
import com.travis.monolith.system.message.internal.entity.SysMessage;
import com.travis.monolith.system.message.internal.model.SysMessageAudience;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 事务提交后广播消息收件箱变化。 */
@Component
@RequiredArgsConstructor
public class SysMessageInboxNotifier {
    private final WebSocketMessageSender webSocketMessageSender;

    /** 按消息当前及补充接收范围通知收件箱变化。 */
    public void notify(
            SysMessageWebSocketEvent event,
            SysMessage message,
            SysMessageAudience... additionalAudiences) {
        if (!SysMessageChannel.IN_APP.getValue().equals(message.getChannel())) {
            return;
        }
        var audiences = new LinkedHashSet<SysMessageAudience>();
        audiences.add(SysMessageAudience.from(message));
        Arrays.stream(additionalAudiences).filter(Objects::nonNull).forEach(audiences::add);
        notify(event, message.getId(), audiences.toArray(SysMessageAudience[]::new));
    }

    /** 按指定接收范围通知收件箱变化。 */
    public void notify(
            SysMessageWebSocketEvent event, Long messageId, SysMessageAudience... audiences) {
        var validAudiences = Arrays.stream(audiences).filter(Objects::nonNull).toList();
        if (validAudiences.isEmpty()) {
            return;
        }
        send(event, messageId, validAudiences);
    }

    /** 通知指定用户重新获取收件箱状态。 */
    public void notifyUser(String receiverType, Long userId) {
        var principal = SaTokenWebSocketPrincipal.build(receiverType, userId);
        webSocketMessageSender.sendToPrincipal(
                principal,
                WebSocketMessage.toPrincipal(
                        WebSocketSender.SYSTEM, principal, SysMessageWebSocketEvent.INBOX_CHANGED));
    }

    private void send(
            SysMessageWebSocketEvent event, Long messageId, List<SysMessageAudience> audiences) {
        audiences.stream()
                .map(SysMessageAudience::receiverType)
                .distinct()
                .forEach(
                        receiverType ->
                                webSocketMessageSender.sendToNamespace(
                                        receiverType,
                                        WebSocketMessage.toNamespace(
                                                WebSocketSender.SYSTEM,
                                                receiverType,
                                                event,
                                                Map.of("messageId", messageId))));
    }
}
