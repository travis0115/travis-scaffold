package com.travis.monolith.system.message.internal.api;

import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.monolith.system.message.api.SysMessageApi;
import com.travis.monolith.system.message.api.request.SysMessageCreateReq;
import com.travis.monolith.system.message.internal.service.SysMessageService;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
        request.setMessageType(1);
        request.setPushType(0);
        request.setSourceType(sourceType);
        request.setSourceId(sourceId);
        request.setChannel("IN_APP");
        request.setEnableInboxCopy(true);
        request.setReceiverType(receiverType);
        request.setReceiverScope(1);
        request.setReceiverValues(List.copyOf(userIds));
        Long messageId = messageService.create(request);
        messageService.push(messageId);
    }
}
