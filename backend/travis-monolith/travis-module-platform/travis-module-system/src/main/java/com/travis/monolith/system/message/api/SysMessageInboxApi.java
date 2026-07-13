package com.travis.monolith.system.message.api;

import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.message.api.request.SysUserMessagePageReq;
import com.travis.monolith.system.message.api.response.SysUserMessageDetailResp;
import com.travis.monolith.system.message.api.response.SysUserMessageRecentResp;
import com.travis.monolith.system.message.api.response.SysUserMessageResp;
import java.util.List;

/** 消息收件箱对外 API。 */
public interface SysMessageInboxApi {

    List<SysUserMessageRecentResp> listRecent(String receiverType, Long userId, Integer limit);

    PageResp<SysUserMessageResp> page(String receiverType, Long userId, SysUserMessagePageReq req);

    SysUserMessageDetailResp get(String receiverType, Long userId, Long id);

    Long countUnread(String receiverType, Long userId);

    void markRead(String receiverType, Long userId, Long id);

    void markAllRead(String receiverType, Long userId);

    void delete(String receiverType, Long userId, Long id);

    void clear(String receiverType, Long userId);
}
