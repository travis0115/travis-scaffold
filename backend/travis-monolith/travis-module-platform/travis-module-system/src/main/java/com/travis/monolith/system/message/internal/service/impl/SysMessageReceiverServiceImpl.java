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
import com.travis.infrastructure.framework.websocket.core.sender.WebSocketMessageSender;
import com.travis.monolith.system.file.api.SysFileApi;
import com.travis.monolith.system.message.api.SysMessageSourceContentProvider;
import com.travis.monolith.system.message.api.enums.SysMessageReadStatus;
import com.travis.monolith.system.message.api.enums.SysMessageReceiverScope;
import com.travis.monolith.system.message.api.enums.SysMessageStatus;
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
import java.util.function.Function;
import java.util.stream.Collectors;
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
public class SysMessageReceiverServiceImpl
        extends ServiceImplX<SysMessageReceiverMapper, SysMessageReceiver>
        implements SysMessageReceiverService {
    private static final int STATE_BATCH_SIZE = 500;

    private final SysMessageReceiverConverter converter;
    private final SysUserApi userApi;
    private final SysRoleApi roleApi;
    private final WebSocketMessageSender webSocketMessageSender;
    private final SysFileApi fileApi;
    private final Map<String, SysMessageSourceContentProvider> sourceContentProviders;

    public SysMessageReceiverServiceImpl(
            SysMessageReceiverConverter converter,
            SysUserApi userApi,
            SysRoleApi roleApi,
            WebSocketMessageSender webSocketMessageSender,
            SysFileApi fileApi,
            List<SysMessageSourceContentProvider> sourceContentProviders) {
        this.converter = converter;
        this.userApi = userApi;
        this.roleApi = roleApi;
        this.webSocketMessageSender = webSocketMessageSender;
        this.fileApi = fileApi;
        this.sourceContentProviders =
                sourceContentProviders.stream()
                        .collect(
                                Collectors.toMap(
                                        SysMessageSourceContentProvider::getSourceType,
                                        Function.identity()));
    }

    @Override
    public List<SysUserMessageResp> listRecent(Long userId, Integer limit) {
        return listRecent(LoginType.ADMIN, userId, limit);
    }

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

    @Override
    public PageResp<SysUserMessageResp> page(Long userId, SysUserMessagePageReq req) {
        return page(LoginType.ADMIN, userId, req);
    }

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

    @Override
    public SysUserMessageResp getOrThrow(Long userId, Long id) {
        return getOrThrow(LoginType.ADMIN, userId, id);
    }

    @Override
    public SysUserMessageResp getOrThrow(String receiverType, Long userId, Long id) {
        ensureVisible(receiverType, userId, id);
        var message = baseMapper.selectMessageById(id);
        var resp = converter.toResp(message, getState(receiverType, userId, id));
        var provider = sourceContentProviders.get(message.getSourceType());
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

    @Override
    @Cacheable(key = "'unread:'+#userId")
    public Long countUnread(Long userId) {
        return countUnread(LoginType.ADMIN, userId);
    }

    @Override
    @Cacheable(key = "'unread:'+#receiverType+':' + #userId")
    public Long countUnread(String receiverType, Long userId) {
        var context = audienceContext(receiverType, userId);
        return baseMapper.countUnreadInbox(
                userId, receiverType, context.roleIds(), context.deptId());
    }

    @Override
    @Transactional
    @CacheEvict(key = "'unread:'+#userId")
    public void markRead(Long userId, Long id) {
        markRead(LoginType.ADMIN, userId, id);
    }

    @Override
    @Transactional
    @CacheEvict(key = "'unread:'+#receiverType+':' + #userId")
    public void markRead(String receiverType, Long userId, Long id) {
        ensureVisible(receiverType, userId, id);
        upsertState(receiverType, userId, id, SysMessageReadStatus.READ.getValue());
    }

    @Override
    @Transactional
    @CacheEvict(key = "'unread:'+#userId")
    public void markAllRead(Long userId) {
        markAllRead(LoginType.ADMIN, userId);
    }

    @Override
    @Transactional
    @CacheEvict(key = "'unread:'+#receiverType+':' + #userId")
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

    @Override
    @Transactional
    @CacheEvict(key = "'unread:'+#userId")
    public void delete(Long userId, Long id) {
        delete(LoginType.ADMIN, userId, id);
    }

    @Override
    @Transactional
    @CacheEvict(key = "'unread:'+#receiverType+':' + #userId")
    public void delete(String receiverType, Long userId, Long id) {
        ensureVisible(receiverType, userId, id);
        upsertState(receiverType, userId, id, SysMessageReadStatus.DELETED.getValue());
        notifyInboxChanged();
    }

    @Override
    @Transactional
    @CacheEvict(key = "'unread:'+#userId")
    public void clear(Long userId) {
        clear(LoginType.ADMIN, userId);
    }

    @Override
    @Transactional
    @CacheEvict(key = "'unread:'+#receiverType+':' + #userId")
    public void clear(String receiverType, Long userId) {
        var context = audienceContext(receiverType, userId);
        var messageIds =
                baseMapper.selectInboxMessageIds(
                        userId, receiverType, context.roleIds(), context.deptId(), null);
        batchUpsertStates(
                receiverType, userId, messageIds, SysMessageReadStatus.DELETED.getValue());
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void deleteByMessageId(Long messageId) {
        baseMapper.deleteByMessageId(messageId);
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void resetReadStatus(Long messageId) {
        Integer unreadStatus = SysMessageReadStatus.UNREAD.getValue();
        update(
                new LambdaUpdateWrapper<SysMessageReceiver>()
                        .eq(SysMessageReceiver::getMessageId, messageId)
                        .ne(SysMessageReceiver::getReadStatus, unreadStatus)
                        .set(SysMessageReceiver::getReadStatus, unreadStatus)
                        .set(SysMessageReceiver::getReadTime, null));
    }

    private LambdaQueryWrapperX<SysMessageReceiver> baseWrapper(String receiverType, Long userId) {
        return new LambdaQueryWrapperX<SysMessageReceiver>()
                .eq(SysMessageReceiver::getReceiverType, receiverType)
                .eq(SysMessageReceiver::getReceiverId, userId);
    }

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

    private List<MessageWithState> toResponses(
            List<SysMessage> messages, Map<Long, SysMessageReceiver> stateMap) {
        return messages.stream()
                .map(message -> new MessageWithState(message, stateMap.get(message.getId())))
                .toList();
    }

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

    private SysMessageReceiver getState(String receiverType, Long userId, Long messageId) {
        return getOne(
                baseWrapper(receiverType, userId).eq(SysMessageReceiver::getMessageId, messageId),
                false);
    }

    private AudienceContext audienceContext(String receiverType, Long userId) {
        if (!LoginType.ADMIN.equals(receiverType)) {
            return new AudienceContext(List.of(), null);
        }
        return new AudienceContext(
                roleApi.getRoleIdsByUserId(userId), userApi.getDeptIdByUserId(userId));
    }

    private List<Long> parseReceiverValues(String receiverValues) {
        if (receiverValues == null || receiverValues.isBlank()) {
            return List.of();
        }
        return JsonUtil.parseArray(receiverValues, Long.class);
    }

    private void notifyInboxChanged() {
        Runnable sender =
                () ->
                        webSocketMessageSender.sendToAll(
                                WebSocketMessage.toAll(
                                        "system", Map.of("event", "SYSTEM_MESSAGE_INBOX_CHANGED")));
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
