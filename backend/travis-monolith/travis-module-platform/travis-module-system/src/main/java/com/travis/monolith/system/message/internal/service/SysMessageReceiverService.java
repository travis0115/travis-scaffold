package com.travis.monolith.system.message.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.message.api.request.SysUserMessagePageReq;
import com.travis.monolith.system.message.api.response.SysUserMessageResp;
import com.travis.monolith.system.message.internal.entity.SysMessageReceiver;
import java.util.List;

/** 消息接收记录服务。 */
public interface SysMessageReceiverService extends IService<SysMessageReceiver> {
    /** 查询指定登录体系用户的最近消息。 */
    List<SysUserMessageResp> listRecent(String receiverType, Long userId, Integer limit);

    /** 分页查询指定登录体系用户的收件箱消息。 */
    PageResp<SysUserMessageResp> page(String receiverType, Long userId, SysUserMessagePageReq req);

    /** 查询指定登录体系用户的消息详情，不存在时抛出业务异常。 */
    SysUserMessageResp getOrThrow(String receiverType, Long userId, Long id);

    /** 统计指定登录体系用户的未读消息数。 */
    Long countUnread(String receiverType, Long userId);

    /** 将指定登录体系用户的一条消息标记为已读。 */
    void markRead(String receiverType, Long userId, Long id);

    /** 将指定登录体系用户的全部消息标记为已读。 */
    void markAllRead(String receiverType, Long userId);

    /** 删除指定登录体系用户的一条收件箱消息。 */
    void delete(String receiverType, Long userId, Long id);

    /** 清空指定登录体系用户的收件箱消息。 */
    void clear(String receiverType, Long userId);

    /** 删除指定消息的全部接收记录。 */
    void deleteByMessageId(Long messageId);

    /** 将指定消息的全部接收记录重置为未读。 */
    void resetReadStatus(Long messageId);
}
