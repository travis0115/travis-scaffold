package com.travis.monolith.system.message.internal.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.jackson.core.JsonUtil;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.infrastructure.framework.websocket.core.message.WebSocketMessage;
import com.travis.infrastructure.framework.websocket.core.message.WebSocketSender;
import com.travis.infrastructure.framework.websocket.core.sender.WebSocketMessageSender;
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
import com.travis.monolith.system.message.internal.mapper.SysMessageReceiverMapper;
import com.travis.monolith.system.message.internal.service.SysMessageReceiverService;
import com.travis.monolith.system.role.api.SysRoleApi;
import com.travis.monolith.system.user.api.SysUserApi;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** 消息接收记录服务实现。 */
@Service
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

    public SysMessageReceiverServiceImpl(
            SysMessageReceiverConverter converter,
            SysUserApi userApi,
            SysRoleApi roleApi,
            WebSocketMessageSender webSocketMessageSender,
            SysFileApi fileApi,
            ObjectProvider<SysMessageSourceContentProvider> sourceContentProviders) {
        this.converter = converter;
        this.userApi = userApi;
        this.roleApi = roleApi;
        this.webSocketMessageSender = webSocketMessageSender;
        this.fileApi = fileApi;
        this.sourceContentProviders = sourceContentProviders;
    }

    /** 查询用户最近可见的消息。 */
    @Override
    public List<SysUserMessageResp> listRecent(String receiverType, Long userId, Integer limit) {
        int actualLimit = limit == null || limit <= 0 ? 10 : Math.min(limit, 50);
        var context = audienceContext(receiverType, userId);
        Page<SysMessage> page =
                baseMapper.selectInboxPage(
                        new Page<>(1, actualLimit, false),
                        userId,
                        receiverType,
                        context.roleIds(),
                        context.deptId(),
                        null,
                        null,
                        null,
                        null,
                        SysMessageReadStatus.UNREAD.getValue());
        return toResponses(page.getRecords(), stateMap(receiverType, userId, page.getRecords()))
                .stream()
                .map(item -> converter.toResp(item.message(), item.receiver()))
                .toList();
    }

    /** 分页查询消息收件状态。 */
    @Override
    public PageResp<SysUserMessageResp> page(
            String receiverType, Long userId, SysUserMessagePageReq req) {
        var context = audienceContext(receiverType, userId);
        Page<SysMessage> page =
                baseMapper.selectInboxPage(
                        new Page<>(req.getPageNum(), req.getPageSize()),
                        userId,
                        receiverType,
                        context.roleIds(),
                        context.deptId(),
                        req.getTitle(),
                        req.getMessageType(),
                        req.getPublishStartDate(),
                        req.getPublishEndDate(),
                        req.getReadStatus());
        Page<SysUserMessageResp> responsePage =
                new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        var stateMap = stateMap(receiverType, userId, page.getRecords());
        responsePage.setRecords(
                toResponses(page.getRecords(), stateMap).stream()
                        .map(item -> converter.toResp(item.message(), item.receiver()))
                        .toList());
        return PageConverter.toResp(responsePage);
    }

    /** 查询指定消息收件状态，不存在时抛出业务异常。 */
    @Override
    public SysUserMessageResp getOrThrow(String receiverType, Long userId, Long id) {
        ensureVisible(receiverType, userId, id);
        var message = baseMapper.selectMessageById(id);
        var resp = converter.toResp(message, getState(receiverType, userId, id));
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
            return resp;
        }
        var sourceContent = provider.get(message.getSourceId());
        resp.setTitle(sourceContent.getTitle());
        resp.setContent(sourceContent.getContent());
        resp.setPublishTime(sourceContent.getPublishTime());
        resp.setMetadata(sourceContent.getMetadata());
        return resp;
    }

    /** 统计用户未读消息数量。 */
    @Override
    public Long countUnread(String receiverType, Long userId) {
        var context = audienceContext(receiverType, userId);
        return baseMapper.countUnreadInbox(
                userId, receiverType, context.roleIds(), context.deptId());
    }

    /** 将指定消息标记为已读。 */
    @Override
    @Transactional
    public void markRead(String receiverType, Long userId, Long id) {
        ensureVisible(receiverType, userId, id);
        upsertState(receiverType, userId, id, SysMessageReadStatus.READ.getValue());
    }

    /** 将用户全部可见消息标记为已读。 */
    @Override
    @Transactional
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
    }

    /** 删除指定消息收件状态。 */
    @Override
    @Transactional
    public void delete(String receiverType, Long userId, Long id) {
        ensureVisible(receiverType, userId, id);
        upsertState(receiverType, userId, id, SysMessageReadStatus.DELETED.getValue());
        notifyInboxChanged();
    }

    /** 清空指定范围内的消息收件状态。 */
    @Override
    @Transactional
    public void clear(String receiverType, Long userId) {
        var context = audienceContext(receiverType, userId);
        var messageIds =
                baseMapper.selectInboxMessageIds(
                        userId, receiverType, context.roleIds(), context.deptId(), null);
        batchUpsertStates(
                receiverType, userId, messageIds, SysMessageReadStatus.DELETED.getValue());
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
        Integer unreadStatus = SysMessageReadStatus.UNREAD.getValue();
        update(
                new LambdaUpdateWrapper<SysMessageReceiver>()
                        .eq(SysMessageReceiver::getMessageId, messageId)
                        .ne(SysMessageReceiver::getReadStatus, unreadStatus)
                        .set(SysMessageReceiver::getReadStatus, unreadStatus)
                        .set(SysMessageReceiver::getReadTime, null));
    }

    /** 构建用户消息收件状态的基础查询条件。 */
    private LambdaQueryWrapperX<SysMessageReceiver> baseWrapper(String receiverType, Long userId) {
        return new LambdaQueryWrapperX<SysMessageReceiver>()
                .eq(SysMessageReceiver::getReceiverType, receiverType)
                .eq(SysMessageReceiver::getReceiverId, userId);
    }

    /** 按消息 ID 组织用户收件状态。 */
    private Map<Long, SysMessageReceiver> stateMap(
            String receiverType, Long userId, List<SysMessage> messages) {
        if (messages.isEmpty()) {
            return Map.of();
        }
        var messageIds = messages.stream().map(SysMessage::getId).distinct().toList();
        return list(
                        baseWrapper(receiverType, userId)
                                .in(SysMessageReceiver::getMessageId, messageIds))
                .stream()
                .collect(Collectors.toMap(SysMessageReceiver::getMessageId, Function.identity()));
    }

    /** 将消息及收件状态转换为用户消息响应。 */
    private List<MessageWithState> toResponses(
            List<SysMessage> messages, Map<Long, SysMessageReceiver> stateMap) {
        return messages.stream()
                .map(message -> new MessageWithState(message, stateMap.get(message.getId())))
                .toList();
    }

    /** 校验指定消息对当前用户可见。 */
    private void ensureVisible(String receiverType, Long userId, Long messageId) {
        var message = baseMapper.selectMessageById(messageId);
        if (message == null
                || !SysMessageStatus.SENT.getValue().equals(message.getStatus())
                || message.getPublishTime() == null
                || message.getPublishTime().isAfter(java.time.LocalDateTime.now())
                || !matchesAudience(receiverType, userId, message)) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        var state = getState(receiverType, userId, messageId);
        if (state != null
                && SysMessageReadStatus.DELETED.getValue().equals(state.getReadStatus())) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
    }

    /** 判断消息接收范围是否匹配当前用户。 */
    private boolean matchesAudience(String receiverType, Long userId, SysMessage message) {
        if (!receiverType.equals(message.getReceiverType())) {
            return false;
        }
        var receiverValues = parseReceiverValues(message.getReceiverValues());
        return SysMessageReceiverScope.findByValue(message.getReceiverScope())
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
    private void upsertState(String receiverType, Long userId, Long messageId, Integer readStatus) {
        var state = getState(receiverType, userId, messageId);
        if (state == null) {
            state = new SysMessageReceiver();
            state.setMessageId(messageId);
            state.setReceiverType(receiverType);
            state.setReceiverId(userId);
        }
        state.setReadStatus(readStatus);
        state.setReadTime(
                SysMessageReadStatus.READ.getValue().equals(readStatus)
                        ? LocalDateTime.now()
                        : null);
        saveOrUpdate(state);
    }

    /** 批量新增或更新用户消息收件状态。 */
    private void batchUpsertStates(
            String receiverType, Long userId, List<Long> messageIds, Integer readStatus) {
        for (int fromIndex = 0; fromIndex < messageIds.size(); fromIndex += STATE_BATCH_SIZE) {
            var batch =
                    messageIds.subList(
                            fromIndex, Math.min(fromIndex + STATE_BATCH_SIZE, messageIds.size()));
            var existingStates =
                    list(
                            baseWrapper(receiverType, userId)
                                    .in(SysMessageReceiver::getMessageId, batch));
            var existingStateMap =
                    existingStates.stream()
                            .collect(
                                    Collectors.toMap(
                                            SysMessageReceiver::getMessageId, Function.identity()));
            var newStates = new ArrayList<SysMessageReceiver>();
            var readTime =
                    SysMessageReadStatus.READ.getValue().equals(readStatus)
                            ? LocalDateTime.now()
                            : null;
            for (Long messageId : batch) {
                var state = existingStateMap.get(messageId);
                if (state == null) {
                    state = new SysMessageReceiver();
                    state.setMessageId(messageId);
                    state.setReceiverType(receiverType);
                    state.setReceiverId(userId);
                    newStates.add(state);
                }
                state.setReadStatus(readStatus);
                state.setReadTime(readTime);
            }
            if (!existingStates.isEmpty()) {
                updateBatchById(existingStates);
            }
            if (!newStates.isEmpty()) {
                saveBatch(newStates);
            }
        }
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
    private void notifyInboxChanged() {
        Runnable sender =
                () ->
                        webSocketMessageSender.sendToAll(
                                WebSocketMessage.toAll(
                                        WebSocketSender.SYSTEM,
                                        SysMessageWebSocketEvent.INBOX_CHANGED));
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            sender.run();
                        }
                    });
        } else {
            sender.run();
        }
    }

    private record AudienceContext(List<Long> roleIds, Long deptId) {}

    private record MessageWithState(SysMessage message, SysMessageReceiver receiver) {}
}
