package com.travis.monolith.system.message.internal.api;

import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.message.api.SysMessageInboxApi;
import com.travis.monolith.system.message.api.request.SysUserMessagePageReq;
import com.travis.monolith.system.message.api.response.SysUserMessageResp;
import com.travis.monolith.system.message.internal.service.SysMessageReceiverService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 消息收件箱对外 API 默认实现。 */
@Component
@RequiredArgsConstructor
public class SysMessageInboxApiImpl implements SysMessageInboxApi {

    private final SysMessageReceiverService messageReceiverService;

    @Override
    public List<SysUserMessageResp> listRecent(
            String receiverType, Long userId, Integer limit) {
        return messageReceiverService.listRecent(receiverType, userId, limit);
    }

    @Override
    public PageResp<SysUserMessageResp> page(
            String receiverType, Long userId, SysUserMessagePageReq req) {
        return messageReceiverService.page(receiverType, userId, req);
    }

    @Override
    public SysUserMessageResp get(String receiverType, Long userId, Long id) {
        return messageReceiverService.get(receiverType, userId, id);
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
