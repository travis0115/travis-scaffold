package com.travis.monolith.system.message.internal.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.jackson.core.JsonUtil;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.infrastructure.framework.satoken.core.websocket.SaTokenWebSocketPrincipal;
import com.travis.infrastructure.framework.websocket.core.message.WebSocketMessage;
import com.travis.infrastructure.framework.websocket.core.message.WebSocketSender;
import com.travis.infrastructure.framework.websocket.core.sender.WebSocketMessageSender;
import com.travis.monolith.system.common.api.enums.SystemErrorCode;
import com.travis.monolith.system.file.api.SysFileApi;
import com.travis.monolith.system.message.api.SysMessageSourceContentProvider;
import com.travis.monolith.system.message.api.enums.SysMessageReadStatus;
import com.travis.monolith.system.message.api.enums.SysMessageReceiverScope;
import com.travis.monolith.system.message.api.enums.SysMessageStatus;
import com.travis.monolith.system.message.api.enums.SysMessageWebSocketEvent;
import com.travis.monolith.system.message.api.request.SysUserMessagePageReq;
import com.travis.monolith.system.message.api.response.SysUserMessageResp;
import com.travis.monolith.system.message.internal.converter.SysMessageReceiverConverter;
import com.travis.monolith.system.message.internal.entity.SysMessage;
import com.travis.monolith.system.message.internal.entity.SysMessageReceiver;
import com.travis.monolith.system.message.internal.mapper.SysMessageMapper;
import com.travis.monolith.system.message.internal.mapper.SysMessageReceiverMapper;
import com.travis.monolith.system.message.internal.service.SysMessageReceiverService;
import com.travis.monolith.system.role.api.SysRoleApi;
import com.travis.monolith.system.user.api.SysUserApi;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** 消息接收记录服务实现。 */
@Service
@CacheConfig(cacheNames = "system:message:inbox")
@AllArgsConstructor
public class SysMessageReceiverServiceImpl
        extends ServiceImplX<SysMessageReceiverMapper, SysMessageReceiver>
        implements SysMessageReceiverService {
    /** 批量更新接收记录状态时的单批数量。 */
    private static final int STATE_BATCH_SIZE = 500;

    private final SysMessageReceiverConverter converter;
    private final SysUserApi userApi;
    private final SysRoleApi roleApi;
    private final WebSocketMessageSender webSocketMessageSender;
    private final SysFileApi fileApi;
    private final ObjectProvider<SysMessageSourceContentProvider> sourceContentProviders;
    private final SysMessageMapper sysMessageMapper;

    /** 查询用户最近可见的消息。 */
    @Override
    public List<SysUserMessageResp> listRecent(String receiverType, Long userId, Integer limit) {
        int actualLimit = limit == null || limit <= 0 ? 10 : Math.min(limit, 50);
        var context = audienceContext(receiverType, userId);
        var page =
                baseMapper.selectInboxPage(
                        new Page<SysUserMessageResp>(1, actualLimit, false),
                        userId,
                        receiverType,
                        context.roleIds(),
                        context.deptId(),
                        null,
                        null,
                        null,
                        null,
                        SysMessageReadStatus.UNREAD.getValue());
        return page.getRecords();
    }

    /** 分页查询消息收件状态。 */
    @Override
    public PageResp<SysUserMessageResp> page(
            String receiverType, Long userId, SysUserMessagePageReq req) {
        var context = audienceContext(receiverType, userId);
        var page =
                baseMapper.selectInboxPage(
                        new Page<SysUserMessageResp>(req.getPageNum(), req.getPageSize()),
                        userId,
                        receiverType,
                        context.roleIds(),
                        context.deptId(),
                        req.getTitle(),
                        req.getMessageType(),
                        req.getPublishStartDate(),
                        req.getPublishEndDate(),
                        req.getReadStatus());
        return PageConverter.toResp(page);
    }

    /** 查询指定消息详情，并在首次查看时标记为已读。 */
    @Override
    @Transactional
    @CacheEvict(key = "'unread:' + #receiverType + ':' + #userId")
    public SysUserMessageResp getAndMarkRead(String receiverType, Long userId, Long id) {
        var message = sysMessageMapper.selectById(id);
        if (message == null) {
            throw new BizException(SystemErrorCode.MESSAGE_NOT_FOUND);
        }
        var state = getState(receiverType, userId, id);
        ensureVisible(receiverType, userId, message, state);
        var stateChange =
                upsertState(receiverType, userId, id, state, SysMessageReadStatus.READ.getValue());
        var resp = converter.toResp(message, stateChange.state());
        var provider =
                sourceContentProviders.stream()
                        .filter(
                                candidate ->
                                        Objects.equals(
                                                candidate.getSourceType(), message.getSourceType()))
                        .findFirst()
                        .orElse(null);
        if (provider == null) {
            resp.setContent(fileApi.resolveManagedImageSources(message.getContent()));
            if (stateChange.changed()) {
                notifyInboxChanged(receiverType, userId);
            }
            return resp;
        }
        var sourceContent = provider.get(message.getSourceId());
        resp.setTitle(sourceContent.getTitle());
        resp.setContent(sourceContent.getContent());
        resp.setPublishTime(sourceContent.getPublishTime());
        resp.setMetadata(sourceContent.getMetadata());
        if (stateChange.changed()) {
            notifyInboxChanged(receiverType, userId);
        }
        return resp;
    }

    /** 统计用户未读消息数量。 */
    @Override
    @Cacheable(key = "'unread:' + #receiverType + ':' + #userId")
    public Long countUnread(String receiverType, Long userId) {
        var context = audienceContext(receiverType, userId);
        return baseMapper.countUnreadInbox(
                userId, receiverType, context.roleIds(), context.deptId());
    }

    /** 将指定消息标记为已读。 */
    @Override
    @Transactional
    @CacheEvict(key = "'unread:' + #receiverType + ':' + #userId")
    public void markRead(String receiverType, Long userId, Long id) {
        var state = ensureVisible(receiverType, userId, id);
        var stateChange =
                upsertState(receiverType, userId, id, state, SysMessageReadStatus.READ.getValue());
        if (stateChange.changed()) {
            notifyInboxChanged(receiverType, userId);
        }
    }

    /** 将用户全部可见消息标记为已读。 */
    @Override
    @Transactional
    @CacheEvict(key = "'unread:' + #receiverType + ':' + #userId")
    public void markAllRead(String receiverType, Long userId) {
        var context = audienceContext(receiverType, userId);
        var messageIds =
                baseMapper.selectInboxMessageIds(
                        userId,
                        receiverType,
                        context.roleIds(),
                        context.deptId(),
                        SysMessageReadStatus.UNREAD.getValue());
        batchUpsertStates(receiverType, userId, messageIds, SysMessageReadStatus.READ.getValue());
        if (!messageIds.isEmpty()) {
            notifyInboxChanged(receiverType, userId);
        }
    }

    /** 删除指定消息收件状态。 */
    @Override
    @Transactional
    @CacheEvict(key = "'unread:' + #receiverType + ':' + #userId")
    public void delete(String receiverType, Long userId, Long id) {
        var state = ensureVisible(receiverType, userId, id);
        upsertState(receiverType, userId, id, state, SysMessageReadStatus.DELETED.getValue());
        notifyInboxChanged(receiverType, userId);
    }

    /** 清空指定范围内的消息收件状态。 */
    @Override
    @Transactional
    @CacheEvict(key = "'unread:' + #receiverType + ':' + #userId")
    public void clear(String receiverType, Long userId) {
        var context = audienceContext(receiverType, userId);
        var messageIds =
                baseMapper.selectInboxMessageIds(
                        userId, receiverType, context.roleIds(), context.deptId(), null);
        batchUpsertStates(
                receiverType, userId, messageIds, SysMessageReadStatus.DELETED.getValue());
        if (!messageIds.isEmpty()) {
            notifyInboxChanged(receiverType, userId);
        }
    }

    /** 删除指定消息的全部收件状态。 */
    @Override
    @Transactional
    public void deleteByMessageId(Long messageId) {
        baseMapper.deleteByMessageId(messageId);
    }

    /** 重置指定消息的用户已读状态。 */
    @Override
    @Transactional
    public void resetReadStatus(Long messageId) {
        var unreadStatus = SysMessageReadStatus.UNREAD.getValue();
        update(
                new LambdaUpdateWrapper<SysMessageReceiver>()
                        .eq(SysMessageReceiver::getMessageId, messageId)
                        .ne(SysMessageReceiver::getReadStatus, unreadStatus)
                        .set(SysMessageReceiver::getReadStatus, unreadStatus)
                        .set(SysMessageReceiver::getReadTime, null));
    }

    /** 清除全部收件箱未读数缓存。 */
    @Override
    @CacheEvict(allEntries = true)
    public void evictUnreadCache() {}

    /** 构建用户消息收件状态的基础查询条件。 */
    private LambdaQueryWrapperX<SysMessageReceiver> baseWrapper(String receiverType, Long userId) {
        return new LambdaQueryWrapperX<SysMessageReceiver>()
                .eq(SysMessageReceiver::getReceiverType, receiverType)
                .eq(SysMessageReceiver::getReceiverId, userId);
    }

    /** 校验指定消息对当前用户可见。 */
    private SysMessageReceiver ensureVisible(String receiverType, Long userId, Long messageId) {
        var message = sysMessageMapper.selectById(messageId);
        if (message == null) {
            throw new BizException(SystemErrorCode.MESSAGE_NOT_FOUND);
        }

        var state = getState(receiverType, userId, messageId);
        ensureVisible(receiverType, userId, message, state);
        return state;
    }

    /** 校验指定消息对当前用户可见。 */
    private void ensureVisible(
            String receiverType, Long userId, SysMessage message, SysMessageReceiver state) {
        ensureVisible(
                message.getStatus(),
                message.getPublishTime(),
                matchesAudience(
                        receiverType,
                        userId,
                        message.getReceiverType(),
                        message.getReceiverScope(),
                        parseReceiverValues(message.getReceiverValues())),
                state);
    }

    /** 校验消息发布状态、接收范围和用户收件状态。 */
    private void ensureVisible(
            Integer status,
            LocalDateTime publishTime,
            boolean audienceMatched,
            SysMessageReceiver state) {
        if (!SysMessageStatus.SENT.getValue().equals(status)
                || publishTime == null
                || publishTime.isAfter(LocalDateTime.now())
                || !audienceMatched
                || (state != null
                        && SysMessageReadStatus.DELETED.getValue().equals(state.getReadStatus()))) {
            throw new BizException(SystemErrorCode.MESSAGE_NOT_FOUND);
        }
    }

    /** 判断消息接收范围是否匹配当前用户。 */
    private boolean matchesAudience(
            String receiverType,
            Long userId,
            String messageReceiverType,
            Integer receiverScope,
            List<Long> receiverValues) {
        if (!receiverType.equals(messageReceiverType)) {
            return false;
        }
        return SysMessageReceiverScope.findByValue(receiverScope)
                .map(
                        scope ->
                                switch (scope) {
                                    case ALL -> true;
                                    case USER -> receiverValues.contains(userId);
                                    case ROLE ->
                                            roleApi.getRoleIdsByUserId(userId).stream()
                                                    .anyMatch(receiverValues::contains);
                                    case DEPT ->
                                            receiverValues.contains(
                                                    userApi.getDeptIdByUserId(userId));
                                })
                .orElse(false);
    }

    /** 新增或更新单条用户消息收件状态。 */
    private StateChange upsertState(
            String receiverType,
            Long userId,
            Long messageId,
            SysMessageReceiver state,
            Integer readStatus) {
        if (state != null
                && SysMessageReadStatus.READ.getValue().equals(readStatus)
                && SysMessageReadStatus.READ.getValue().equals(state.getReadStatus())) {
            return new StateChange(state, false);
        }
        var targetState =
                newState(receiverType, userId, messageId, readStatus, LocalDateTime.now());
        baseMapper.upsertStates(List.of(targetState));
        return new StateChange(targetState, true);
    }

    /** 批量新增或更新用户消息收件状态。 */
    private void batchUpsertStates(
            String receiverType, Long userId, List<Long> messageIds, Integer readStatus) {
        var readTime =
                SysMessageReadStatus.READ.getValue().equals(readStatus)
                        ? LocalDateTime.now()
                        : null;
        for (int fromIndex = 0; fromIndex < messageIds.size(); fromIndex += STATE_BATCH_SIZE) {
            var batch =
                    messageIds.subList(
                            fromIndex, Math.min(fromIndex + STATE_BATCH_SIZE, messageIds.size()));
            var states =
                    batch.stream()
                            .map(
                                    messageId ->
                                            newState(
                                                    receiverType,
                                                    userId,
                                                    messageId,
                                                    readStatus,
                                                    readTime))
                            .toList();
            baseMapper.upsertStates(states);
        }
    }

    /** 构建用于原子写入的用户消息状态。 */
    private SysMessageReceiver newState(
            String receiverType,
            Long userId,
            Long messageId,
            Integer readStatus,
            LocalDateTime readTime) {
        var state =
                SysMessageReceiver.builder()
                        .messageId(messageId)
                        .receiverType(receiverType)
                        .receiverId(userId)
                        .readStatus(readStatus)
                        .readTime(
                                SysMessageReadStatus.READ.getValue().equals(readStatus)
                                        ? readTime
                                        : null)
                        .build();
        state.setId(IdWorker.getId());
        return state;
    }

    /** 查询用户对指定消息的收件状态。 */
    private SysMessageReceiver getState(String receiverType, Long userId, Long messageId) {
        return getOne(
                baseWrapper(receiverType, userId).eq(SysMessageReceiver::getMessageId, messageId),
                false);
    }

    /** 查询用户的角色和部门接收范围上下文。 */
    private AudienceContext audienceContext(String receiverType, Long userId) {
        if (!LoginType.ADMIN.equals(receiverType)) {
            return new AudienceContext(List.of(), null);
        }
        return new AudienceContext(
                roleApi.getRoleIdsByUserId(userId), userApi.getDeptIdByUserId(userId));
    }

    /** 解析消息接收对象 ID 列表。 */
    private List<Long> parseReceiverValues(String receiverValues) {
        if (receiverValues == null || receiverValues.isBlank()) {
            return List.of();
        }
        return JsonUtil.parseArray(receiverValues, Long.class);
    }

    /** 在事务提交后通过 WebSocket 通知收件箱发生变化。 */
    private void notifyInboxChanged(String receiverType, Long userId) {
        var principal = SaTokenWebSocketPrincipal.build(receiverType, userId);
        Runnable sender =
                () ->
                        webSocketMessageSender.sendToPrincipal(
                                principal,
                                WebSocketMessage.toPrincipal(
                                        WebSocketSender.SYSTEM,
                                        principal,
                                        SysMessageWebSocketEvent.INBOX_CHANGED));
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCompletion(int status) {
                            if (status == TransactionSynchronization.STATUS_COMMITTED) {
                                sender.run();
                            }
                        }
                    });
        } else {
            sender.run();
        }
    }

    private record AudienceContext(List<Long> roleIds, Long deptId) {}

    private record StateChange(SysMessageReceiver state, boolean changed) {}
}
