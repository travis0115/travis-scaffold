package com.travis.monolith.system.notice.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.notice.api.request.SysUserMessagePageReq;
import com.travis.monolith.system.notice.api.response.SysUserMessageResp;
import com.travis.monolith.system.notice.internal.entity.SysUserMessage;
import java.util.List;

public interface SysUserMessageService extends IService<SysUserMessage> {
    List<SysUserMessageResp> listRecent(Long userId, Integer limit);

    PageResp<SysUserMessageResp> page(Long userId, SysUserMessagePageReq req);

    long countUnread(Long userId);

    void markRead(Long userId, Long id);

    void markAllRead(Long userId);

    void delete(Long userId, Long id);

    void clear(Long userId);
}
