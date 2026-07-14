package com.travis.monolith.system.message.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.message.api.request.SysMessageCreateReq;
import com.travis.monolith.system.message.api.request.SysMessagePageReq;
import com.travis.monolith.system.message.api.request.SysMessageUpdateReq;
import com.travis.monolith.system.message.api.request.SysSourceMessagePublishReq;
import com.travis.monolith.system.message.api.response.SysMessageResp;
import com.travis.monolith.system.message.internal.entity.SysMessage;

/** 消息推送服务。 */
public interface SysMessageService extends IService<SysMessage> {
    PageResp<SysMessageResp> page(SysMessagePageReq req);

    SysMessageResp getOrThrow(Long id);

    Long create(SysMessageCreateReq req);

    void update(Long id, SysMessageUpdateReq req);

    void updateStatus(Long id, Integer status);

    void push(Long id);

    void pushAutomatic(Long id);

    void revoke(Long id);

    void publishSourceMessage(SysSourceMessagePublishReq req);

    void revokeSourceMessage(String sourceType, String sourceId, String receiverType);

    void deleteSourceMessage(String sourceType, String sourceId, String receiverType);

    int pushDueScheduledMessages();

    void delete(Long id);
}
