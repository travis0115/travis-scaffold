package com.travis.monolith.system.message.internal.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.common.web.model.PageResp;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@CacheConfig(cacheNames = "system:message-inbox")
public class SysMessageReceiverServiceImpl
        extends ServiceImplX<SysMessageReceiverMapper, SysMessageReceiver>
        implements SysMessageReceiverService {
    private static final String RECEIVER_TYPE_ADMIN = LoginType.ADMIN;
    private static final int AUDIENCE_ALL = 0;
    private static final int AUDIENCE_USER = 1;
    private static final int AUDIENCE_ROLE = 2;
    private static final int AUDIENCE_DEPT = 3;
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
        int actualLimit = limit == null || limit <= 0 ? 10 : Math.min(limit, 50);
        var context = audienceContext(userId);
        Page<SysMessage> page =
                messageMapper.selectInboxPage(
                        new Page<>(1, actualLimit),
                        userId,
                        RECEIVER_TYPE_ADMIN,
                        context.roleIds(),
                        context.deptId(),
                        null,
                        null);
        return toResponses(page.getRecords(), stateMap(userId, page.getRecords())).stream()
                .map(item -> converter.toRecentResp(item.message(), item.receiver()))
                .toList();
    }

    @Override
    public PageResp<SysUserMessagePageResp> page(Long userId, SysUserMessagePageReq req) {
        var context = audienceContext(userId);
        Page<SysMessage> page =
                messageMapper.selectInboxPage(
                        new Page<>(req.getPageNum(), req.getPageSize()),
                        userId,
                        RECEIVER_TYPE_ADMIN,
                        context.roleIds(),
                        context.deptId(),
                        req.getTitle(),
                        req.getReadStatus());
        Page<SysUserMessagePageResp> responsePage =
                new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        var stateMap = stateMap(userId, page.getRecords());
        responsePage.setRecords(
                toResponses(page.getRecords(), stateMap).stream()
                        .map(item -> converter.toPageResp(item.message(), item.receiver()))
                        .toList());
        return PageConverter.toResp(responsePage);
    }

    @Override
    @Cacheable(key = "'unread:'+#userId")
    public long countUnread(Long userId) {
        var context = audienceContext(userId);
        return messageMapper.countUnreadInbox(
                userId, RECEIVER_TYPE_ADMIN, context.roleIds(), context.deptId());
    }

    @Override
    @Transactional
    @CacheEvict(key = "'unread:'+#userId")
    public void markRead(Long userId, Long id) {
        ensureVisible(userId, id);
        upsertState(userId, id, READ_READ);
    }

    @Override
    @Transactional
    @CacheEvict(key = "'unread:'+#userId")
    public void markAllRead(Long userId) {
        var context = audienceContext(userId);
        messageMapper
                .selectInboxMessageIds(
                        userId,
                        RECEIVER_TYPE_ADMIN,
                        context.roleIds(),
                        context.deptId(),
                        READ_UNREAD)
                .forEach(messageId -> upsertState(userId, messageId, READ_READ));
    }

    @Override
    @Transactional
    @CacheEvict(key = "'unread:'+#userId")
    public void delete(Long userId, Long id) {
        ensureVisible(userId, id);
        upsertState(userId, id, READ_DELETED);
    }

    @Override
    @Transactional
    @CacheEvict(key = "'unread:'+#userId")
    public void clear(Long userId) {
        var context = audienceContext(userId);
        messageMapper
                .selectInboxMessageIds(
                        userId, RECEIVER_TYPE_ADMIN, context.roleIds(), context.deptId(), null)
                .forEach(messageId -> upsertState(userId, messageId, READ_DELETED));
    }

    private LambdaQueryWrapperX<SysMessageReceiver> baseWrapper(Long userId) {
        return new LambdaQueryWrapperX<SysMessageReceiver>()
                .eq(SysMessageReceiver::getReceiverType, RECEIVER_TYPE_ADMIN)
                .eq(SysMessageReceiver::getReceiverId, userId);
    }

    private Map<Long, SysMessageReceiver> stateMap(Long userId, List<SysMessage> messages) {
        if (messages.isEmpty()) {
            return Map.of();
        }
        var messageIds = messages.stream().map(SysMessage::getId).distinct().toList();
        return list(baseWrapper(userId).in(SysMessageReceiver::getMessageId, messageIds)).stream()
                .collect(Collectors.toMap(SysMessageReceiver::getMessageId, Function.identity()));
    }

    private List<MessageWithState> toResponses(
            List<SysMessage> messages, Map<Long, SysMessageReceiver> stateMap) {
        return messages.stream()
                .map(message -> new MessageWithState(message, stateMap.get(message.getId())))
                .toList();
    }

    private void ensureVisible(Long userId, Long messageId) {
        var message = messageMapper.selectById(messageId);
        if (message == null
                || !Integer.valueOf(2).equals(message.getStatus())
                || message.getPublishTime() == null
                || message.getPublishTime().isAfter(java.time.LocalDateTime.now())
                || !matchesAudience(userId, message)) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        var state = getState(userId, messageId);
        if (state != null && Integer.valueOf(READ_DELETED).equals(state.getReadStatus())) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
    }

    private boolean matchesAudience(Long userId, SysMessage message) {
        var targetIds = parseTargetIds(message.getTargetIds());
        return switch (message.getAudienceType()) {
            case AUDIENCE_ALL -> true;
            case AUDIENCE_USER -> targetIds.contains(userId);
            case AUDIENCE_ROLE ->
                    roleApi.getRoleIdsByUserId(userId).stream().anyMatch(targetIds::contains);
            case AUDIENCE_DEPT -> targetIds.contains(userApi.getDeptIdByUserId(userId));
            default -> false;
        };
    }

    private void upsertState(Long userId, Long messageId, Integer readStatus) {
        var state = getState(userId, messageId);
        if (state == null) {
            state = new SysMessageReceiver();
            state.setMessageId(messageId);
            state.setReceiverType(RECEIVER_TYPE_ADMIN);
            state.setReceiverId(userId);
        }
        state.setReadStatus(readStatus);
        state.setReadTime(
                Integer.valueOf(READ_READ).equals(readStatus) ? LocalDateTime.now() : null);
        saveOrUpdate(state);
    }

    private SysMessageReceiver getState(Long userId, Long messageId) {
        return getOne(baseWrapper(userId).eq(SysMessageReceiver::getMessageId, messageId), false);
    }

    private AudienceContext audienceContext(Long userId) {
        return new AudienceContext(
                roleApi.getRoleIdsByUserId(userId), userApi.getDeptIdByUserId(userId));
    }

    private List<Long> parseTargetIds(String targetIds) {
        if (targetIds == null || targetIds.isBlank()) {
            return List.of();
        }
        return Arrays.stream(targetIds.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(Long::valueOf)
                .toList();
    }

    private record AudienceContext(List<Long> roleIds, Long deptId) {}

    private record MessageWithState(SysMessage message, SysMessageReceiver receiver) {}
}
