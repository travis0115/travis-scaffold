package com.travis.monolith.system.message.internal.api;

import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.monolith.system.message.api.SysMessageApi;
import com.travis.monolith.system.message.api.enums.SysMessageChannel;
import com.travis.monolith.system.message.api.enums.SysMessagePushType;
import com.travis.monolith.system.message.api.enums.SysMessageReceiverScope;
import com.travis.monolith.system.message.api.enums.SysMessageType;
import com.travis.monolith.system.message.api.request.SysMessageCreateReq;
import com.travis.monolith.system.message.api.request.SysSourceMessagePublishReq;
import com.travis.monolith.system.message.internal.service.SysMessageService;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 消息推送对外 API 默认实现。 */
@Component
@RequiredArgsConstructor
public class SysMessageApiImpl implements SysMessageApi {

    private final SysMessageService messageService;

    @Override
    public void publishToUsers(String title, String content, Collection<Long> userIds) {
        publishToUsers(LoginType.ADMIN, title, content, userIds, null, null);
    }

    @Override
    public void publishToUsers(
            String title,
            String content,
            Collection<Long> userIds,
            String sourceType,
            String sourceId) {
        publishToUsers(LoginType.ADMIN, title, content, userIds, sourceType, sourceId);
    }

    @Override
    public void publishToUsers(
            String receiverType,
            String title,
            String content,
            Collection<Long> userIds,
            String sourceType,
            String sourceId) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        var request = new SysMessageCreateReq();
        request.setTitle(title);
        request.setContent(content);
        request.setMessageType(SysMessageType.SYSTEM.getValue());
        request.setPushType(SysMessagePushType.MANUAL.getValue());
        request.setSourceType(sourceType);
        request.setSourceId(sourceId);
        request.setChannel(SysMessageChannel.IN_APP.getValue());
        request.setEnableInboxCopy(true);
        request.setReceiverType(receiverType);
        request.setReceiverScope(SysMessageReceiverScope.USER.getValue());
        request.setReceiverValues(List.copyOf(userIds));
        Long messageId = messageService.create(request);
        messageService.pushAutomatic(messageId);
    }

    @Override
    public void publishSourceMessage(SysSourceMessagePublishReq req) {
        messageService.publishSourceMessage(req);
    }

    @Override
    public void revokeSourceMessage(String sourceType, String sourceId, String receiverType) {
        messageService.revokeSourceMessage(sourceType, sourceId, receiverType);
    }

    @Override
    public void deleteSourceMessage(String sourceType, String sourceId, String receiverType) {
        messageService.deleteSourceMessage(sourceType, sourceId, receiverType);
    }
}
