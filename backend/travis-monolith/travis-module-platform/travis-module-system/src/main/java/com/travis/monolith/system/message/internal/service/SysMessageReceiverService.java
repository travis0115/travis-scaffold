package com.travis.monolith.system.message.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.message.api.request.SysUserMessagePageReq;
import com.travis.monolith.system.message.api.response.SysUserMessageResp;
import com.travis.monolith.system.message.api.response.SysUserMessageRecentResp;
import com.travis.monolith.system.message.internal.entity.SysMessageReceiver;
import java.util.List;

/** 消息接收记录服务。 */
public interface SysMessageReceiverService extends IService<SysMessageReceiver> {
    List<SysUserMessageRecentResp> listRecent(Long userId, Integer limit);

    List<SysUserMessageRecentResp> listRecent(String receiverType, Long userId, Integer limit);

    PageResp<SysUserMessageResp> page(Long userId, SysUserMessagePageReq req);

    PageResp<SysUserMessageResp> page(
            String receiverType, Long userId, SysUserMessagePageReq req);

    Long countUnread(Long userId);

    Long countUnread(String receiverType, Long userId);

    void markRead(Long userId, Long id);

    void markRead(String receiverType, Long userId, Long id);

    void markAllRead(Long userId);

    void markAllRead(String receiverType, Long userId);

    void delete(Long userId, Long id);

    void delete(String receiverType, Long userId, Long id);

    void clear(Long userId);

    void clear(String receiverType, Long userId);
}
