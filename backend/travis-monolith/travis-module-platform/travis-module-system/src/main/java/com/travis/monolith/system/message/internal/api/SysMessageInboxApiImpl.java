package com.travis.monolith.system.message.internal.api;

import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.message.api.SysMessageInboxApi;
import com.travis.monolith.system.message.api.request.SysUserMessagePageReq;
import com.travis.monolith.system.message.api.response.SysUserMessagePageResp;
import com.travis.monolith.system.message.api.response.SysUserMessageRecentResp;
import com.travis.monolith.system.message.internal.service.SysMessageReceiverService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SysMessageInboxApiImpl implements SysMessageInboxApi {

    private final SysMessageReceiverService messageReceiverService;

    @Override
    public List<SysUserMessageRecentResp> listRecent(
            String receiverType, Long userId, Integer limit) {
        return messageReceiverService.listRecent(receiverType, userId, limit);
    }

    @Override
    public PageResp<SysUserMessagePageResp> page(
            String receiverType, Long userId, SysUserMessagePageReq req) {
        return messageReceiverService.page(receiverType, userId, req);
    }

    @Override
    public Long countUnread(String receiverType, Long userId) {
        return messageReceiverService.countUnread(receiverType, userId);
    }

    @Override
    public void markRead(String receiverType, Long userId, Long id) {
        messageReceiverService.markRead(receiverType, userId, id);
    }

    @Override
    public void markAllRead(String receiverType, Long userId) {
        messageReceiverService.markAllRead(receiverType, userId);
    }

    @Override
    public void delete(String receiverType, Long userId, Long id) {
        messageReceiverService.delete(receiverType, userId, id);
    }

    @Override
    public void clear(String receiverType, Long userId) {
        messageReceiverService.clear(receiverType, userId);
    }
}
