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
    /** 分页查询消息推送记录。 */
    PageResp<SysMessageResp> page(SysMessagePageReq req);

    /** 查询消息详情，不存在时抛出业务异常。 */
    SysMessageResp getOrThrow(Long id);

    /** 创建消息并返回消息 ID。 */
    Long create(SysMessageCreateReq req);

    /** 创建系统自动消息并返回消息 ID。 */
    Long createSystem(SysMessageCreateReq req);

    /** 更新未发布消息。 */
    void update(Long id, SysMessageUpdateReq req);

    /** 手动推送指定消息。 */
    void push(Long id);

    /** 推送指定的系统自动消息。 */
    void pushAutomatic(Long id);

    /** 撤回已发布消息。 */
    void revoke(Long id);

    /** 按业务来源发布或更新消息。 */
    void publishSourceMessage(SysSourceMessagePublishReq req);

    /** 撤回指定业务来源的消息。 */
    void revokeSourceMessage(String sourceType, String sourceId, String receiverType);

    /** 删除指定业务来源的消息。 */
    void deleteSourceMessage(String sourceType, String sourceId, String receiverType);

    /** 推送指定的到期定时消息，并返回是否实际完成推送。 */
    boolean pushScheduled(Long id);

    /** 删除消息及其接收记录。 */
    void delete(Long id);
}
