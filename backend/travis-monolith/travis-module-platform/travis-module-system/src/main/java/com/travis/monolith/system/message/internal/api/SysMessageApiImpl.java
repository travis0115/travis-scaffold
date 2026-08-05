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
import org.springframework.transaction.annotation.Transactional;

/** 消息推送对外 API 默认实现。 */
@Component
@RequiredArgsConstructor
public class SysMessageApiImpl implements SysMessageApi {

    private final SysMessageService messageService;

    @Override
    @Transactional
    public void publishToUsers(String title, String content, Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        var request = new SysMessageCreateReq();
        request.setTitle(title);
        request.setContent(content);
        request.setMessageType(SysMessageType.SYSTEM.getValue());
        request.setPushType(SysMessagePushType.MANUAL.getValue());
        request.setChannel(SysMessageChannel.IN_APP.getValue());
        request.setReceiverType(LoginType.ADMIN);
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
