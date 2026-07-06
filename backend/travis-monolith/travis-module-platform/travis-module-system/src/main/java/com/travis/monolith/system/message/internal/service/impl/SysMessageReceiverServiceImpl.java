package com.travis.monolith.system.message.internal.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.jackson.core.JsonUtil;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.monolith.system.message.api.request.SysUserMessagePageReq;
import com.travis.monolith.system.message.api.response.SysUserMessagePageResp;
import com.travis.monolith.system.message.api.response.SysUserMessageRecentResp;
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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 消息接收记录服务实现。 */
@Service
@CacheConfig(cacheNames = "system:message-inbox")
public class SysMessageReceiverServiceImpl
        extends ServiceImplX<SysMessageReceiverMapper, SysMessageReceiver>
        implements SysMessageReceiverService {
    private static final int READ_UNREAD = 0;
    private static final int READ_READ = 1;
    private static final int READ_DELETED = 2;

    private final SysMessageMapper messageMapper;
    private final SysMessageReceiverConverter converter;
    private final SysUserApi userApi;
    private final SysRoleApi roleApi;

    public SysMessageReceiverServiceImpl(
            SysMessageMapper messageMapper,
            SysMessageReceiverConverter converter,
            SysUserApi userApi,
            SysRoleApi roleApi) {
        this.messageMapper = messageMapper;
        this.converter = converter;
        this.userApi = userApi;
        this.roleApi = roleApi;
    }

    @Override
    public List<SysUserMessageRecentResp> listRecent(Long userId, Integer limit) {
        return listRecent(LoginType.ADMIN, userId, limit);
    }

    @Override
    public List<SysUserMessageRecentResp> listRecent(
            String receiverType, Long userId, Integer limit) {
        int actualLimit = limit == null || limit <= 0 ? 10 : Math.min(limit, 50);
        var context = audienceContext(receiverType, userId);
        Page<SysMessage> page =
                messageMapper.selectInboxPage(
                        new Page<>(1, actualLimit),
                        userId,
                        receiverType,
                        context.roleIds(),
                        context.deptId(),
                        null,
                        null);
        return toResponses(page.getRecords(), stateMap(receiverType, userId, page.getRecords()))
                .stream()
                .map(item -> converter.toRecentResp(item.message(), item.receiver()))
                .toList();
    }

    @Override
    public PageResp<SysUserMessagePageResp> page(Long userId, SysUserMessagePageReq req) {
        return page(LoginType.ADMIN, userId, req);
    }

    @Override
    public PageResp<SysUserMessagePageResp> page(
            String receiverType, Long userId, SysUserMessagePageReq req) {
        var context = audienceContext(receiverType, userId);
        Page<SysMessage> page =
                messageMapper.selectInboxPage(
                        new Page<>(req.getPageNum(), req.getPageSize()),
                        userId,
                        receiverType,
                        context.roleIds(),
                        context.deptId(),
                        req.getTitle(),
                        req.getReadStatus());
        Page<SysUserMessagePageResp> responsePage =
                new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        var stateMap = stateMap(receiverType, userId, page.getRecords());
        responsePage.setRecords(
                toResponses(page.getRecords(), stateMap).stream()
                        .map(item -> converter.toPageResp(item.message(), item.receiver()))
                        .toList());
        return PageConverter.toResp(responsePage);
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
        return messageMapper.countUnreadInbox(
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
        upsertState(receiverType, userId, id, READ_READ);
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
        messageMapper
                .selectInboxMessageIds(
                        userId, receiverType, context.roleIds(), context.deptId(), READ_UNREAD)
                .forEach(messageId -> upsertState(receiverType, userId, messageId, READ_READ));
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
        upsertState(receiverType, userId, id, READ_DELETED);
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
        messageMapper
                .selectInboxMessageIds(
                        userId, receiverType, context.roleIds(), context.deptId(), null)
                .forEach(messageId -> upsertState(receiverType, userId, messageId, READ_DELETED));
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
        var message = messageMapper.selectById(messageId);
        if (message == null
                || !Integer.valueOf(2).equals(message.getStatus())
                || message.getPublishTime() == null
                || message.getPublishTime().isAfter(java.time.LocalDateTime.now())
                || !matchesAudience(receiverType, userId, message)) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        var state = getState(receiverType, userId, messageId);
        if (state != null && Integer.valueOf(READ_DELETED).equals(state.getReadStatus())) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
    }

    private boolean matchesAudience(String receiverType, Long userId, SysMessage message) {
        if (!receiverType.equals(message.getReceiverType())) {
            return false;
        }
        var receiverValues = parseReceiverValues(message.getReceiverValues());
        return switch (message.getReceiverScope()) {
            case 0 -> true;
            case 1 -> receiverValues.contains(userId);
            case 2 ->
                    roleApi.getRoleIdsByUserId(userId).stream().anyMatch(receiverValues::contains);
            case 3 -> receiverValues.contains(userApi.getDeptIdByUserId(userId));
            default -> false;
        };
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
                Integer.valueOf(READ_READ).equals(readStatus) ? LocalDateTime.now() : null);
        saveOrUpdate(state);
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

    private record AudienceContext(List<Long> roleIds, Long deptId) {}

    private record MessageWithState(SysMessage message, SysMessageReceiver receiver) {}
}
