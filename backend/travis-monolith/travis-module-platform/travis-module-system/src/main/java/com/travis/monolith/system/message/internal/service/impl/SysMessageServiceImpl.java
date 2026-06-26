package com.travis.monolith.system.message.internal.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.infrastructure.framework.websocket.core.WebSocketMessageSender;
import com.travis.infrastructure.framework.websocket.message.WebSocketMessage;
import com.travis.monolith.system.file.api.SysFileApi;
import com.travis.monolith.system.message.api.request.SysMessageCreateReq;
import com.travis.monolith.system.message.api.request.SysMessagePageReq;
import com.travis.monolith.system.message.api.request.SysMessageUpdateReq;
import com.travis.monolith.system.message.api.response.SysMessageChannelContentResp;
import com.travis.monolith.system.message.api.response.SysMessageDetailResp;
import com.travis.monolith.system.message.api.response.SysMessagePageResp;
import com.travis.monolith.system.message.internal.converter.SysMessageConverter;
import com.travis.monolith.system.message.internal.entity.SysMessage;
import com.travis.monolith.system.message.internal.entity.SysMessageChannelContent;
import com.travis.monolith.system.message.internal.mapper.SysMessageChannelContentMapper;
import com.travis.monolith.system.message.internal.mapper.SysMessageMapper;
import com.travis.monolith.system.message.internal.mapper.SysMessageReceiverMapper;
import com.travis.monolith.system.message.internal.service.SysMessageService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@CacheConfig(cacheNames = "system:message")
public class SysMessageServiceImpl extends ServiceImplX<SysMessageMapper, SysMessage>
        implements SysMessageService {
    private static final int RECEIVER_SCOPE_ALL = 0;
    private static final int RECEIVER_SCOPE_USER = 1;
    private static final int RECEIVER_SCOPE_ROLE = 2;
    private static final int RECEIVER_SCOPE_DEPT = 3;
    private static final int PUSH_TYPE_MANUAL = 0;
    private static final int PUSH_TYPE_SCHEDULED = 1;
    private static final int STATUS_PENDING = 0;
    private static final int STATUS_SCHEDULED = 1;
    private static final int STATUS_SENT = 2;
    private static final int STATUS_REVOKED = 3;

    private final SysMessageReceiverMapper messageReceiverMapper;
    private final SysMessageChannelContentMapper channelContentMapper;
    private final SysMessageConverter converter;
    private final WebSocketMessageSender webSocketMessageSender;
    private final SysFileApi fileApi;

    public SysMessageServiceImpl(
            SysMessageReceiverMapper messageReceiverMapper,
            SysMessageChannelContentMapper channelContentMapper,
            SysMessageConverter converter,
            WebSocketMessageSender webSocketMessageSender,
            SysFileApi fileApi) {
        this.messageReceiverMapper = messageReceiverMapper;
        this.channelContentMapper = channelContentMapper;
        this.converter = converter;
        this.webSocketMessageSender = webSocketMessageSender;
        this.fileApi = fileApi;
    }

    @Override
    public PageResp<SysMessagePageResp> page(SysMessagePageReq req) {
        var wrapper =
                new LambdaQueryWrapperX<SysMessage>()
                        .likeIfPresent(SysMessage::getTitle, req.getTitle())
                        .eqIfPresent(SysMessage::getMessageType, req.getMessageType())
                        .eqIfPresent(SysMessage::getPushType, req.getPushType())
                        .eqIfPresent(SysMessage::getStatus, req.getStatus())
                        .orderByDesc(SysMessage::getCreateTime);
        Page<SysMessage> page = page(req.getPageNum(), req.getPageSize(), wrapper);
        return PageConverter.toResp(page.convert(converter::toPageResp));
    }

    @Override
    public SysMessageDetailResp get(Long id) {
        var resp = converter.toDetailResp(getByIdOrThrow(id));
        resp.setContent(fileApi.resolveManagedImageSources(resp.getContent()));
        resp.setChannelContents(listChannelContents(id));
        return resp;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "system:message-inbox", allEntries = true)
    public Long create(SysMessageCreateReq req) {
        validateReceiver(req.getReceiverType(), req.getReceiverScope(), req.getReceiverValues());
        validatePush(req.getPushType(), req.getPublishTime());
        var entity = converter.toEntity(req);
        entity.setStatus(initialStatus(req.getPushType()));
        if (Integer.valueOf(PUSH_TYPE_MANUAL).equals(req.getPushType())) {
            entity.setPublishTime(null);
        }
        entity.setContent(
                fileApi.stripManagedImageSources(
                        resolveInAppContent(req.getChannelContents(), req.getContent())));
        save(entity);
        syncChannelContents(entity.getId(), req);
        return entity.getId();
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(key = "'detail:'+#id"),
                @CacheEvict(cacheNames = "system:message-inbox", allEntries = true)
            })
    public void updateStatus(Long id, Integer status) {
        if (Integer.valueOf(STATUS_SENT).equals(status)) {
            push(id);
            return;
        }
        if (Integer.valueOf(STATUS_REVOKED).equals(status)) {
            revoke(id);
            return;
        }
        throw new BizException(CommonErrorCode.BAD_REQUEST);
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(key = "'detail:'+#id"),
                @CacheEvict(cacheNames = "system:message-inbox", allEntries = true)
            })
    public void update(Long id, SysMessageUpdateReq req) {
        validateReceiver(req.getReceiverType(), req.getReceiverScope(), req.getReceiverValues());
        validatePush(req.getPushType(), req.getPublishTime());
        var entity = getByIdOrThrow(id);
        if (Integer.valueOf(STATUS_SENT).equals(entity.getStatus())) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
        converter.update(req, entity);
        entity.setStatus(initialStatus(req.getPushType()));
        if (Integer.valueOf(PUSH_TYPE_MANUAL).equals(req.getPushType())) {
            entity.setPublishTime(null);
        }
        entity.setContent(
                fileApi.stripManagedImageSources(
                        resolveInAppContent(req.getChannelContents(), req.getContent())));
        updateById(entity);
        syncChannelContents(id, req);
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(key = "'detail:'+#id"),
                @CacheEvict(cacheNames = "system:message-inbox", allEntries = true)
            })
    public void push(Long id) {
        var entity = getByIdOrThrow(id);
        if (Integer.valueOf(STATUS_SENT).equals(entity.getStatus())) {
            return;
        }
        if (!Integer.valueOf(STATUS_PENDING).equals(entity.getStatus())
                && !Integer.valueOf(STATUS_SCHEDULED).equals(entity.getStatus())) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
        publish(entity);
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(key = "'detail:'+#id"),
                @CacheEvict(cacheNames = "system:message-inbox", allEntries = true)
            })
    public void revoke(Long id) {
        var entity = getByIdOrThrow(id);
        if (!Integer.valueOf(STATUS_SENT).equals(entity.getStatus())) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
        entity.setStatus(STATUS_REVOKED);
        updateById(entity);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "system:message-inbox", allEntries = true)
    public int pushDueScheduledMessages() {
        // TODO 后续可改为按消息发布时间创建一次性 Quartz trigger，避免固定频率扫描。
        var messages =
                list(
                        new LambdaQueryWrapperX<SysMessage>()
                                .eq(SysMessage::getStatus, STATUS_SCHEDULED)
                                .le(SysMessage::getPublishTime, LocalDateTime.now())
                                .orderByAsc(SysMessage::getPublishTime));
        messages.forEach(this::publish);
        return messages.size();
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(key = "'detail:'+#id"),
                @CacheEvict(cacheNames = "system:message-inbox", allEntries = true)
            })
    public void delete(Long id) {
        messageReceiverMapper.deleteByMessageId(id);
        channelContentMapper.deleteByMessageId(id);
        removeById(id);
    }

    private List<SysMessageChannelContentResp> listChannelContents(Long messageId) {
        return channelContentMapper
                .selectList(
                        new LambdaQueryWrapperX<SysMessageChannelContent>()
                                .eq(SysMessageChannelContent::getMessageId, messageId)
                                .orderByAsc(SysMessageChannelContent::getId))
                .stream()
                .map(
                        entity -> {
                            var resp = new SysMessageChannelContentResp();
                            BeanUtils.copyProperties(entity, resp);
                            resp.setContent(fileApi.resolveManagedImageSources(resp.getContent()));
                            return resp;
                        })
                .toList();
    }

    private void syncChannelContents(Long messageId, SysMessageCreateReq req) {
        syncChannelContents(messageId, req.getChannelContents());
    }

    private void syncChannelContents(Long messageId, SysMessageUpdateReq req) {
        syncChannelContents(messageId, req.getChannelContents());
    }

    private void syncChannelContents(
            Long messageId,
            List<com.travis.monolith.system.message.api.request.SysMessageChannelContentReq>
                    contents) {
        channelContentMapper.deleteByMessageId(messageId);
        if (contents == null || contents.isEmpty()) {
            return;
        }
        contents.forEach(
                item -> {
                    var entity = new SysMessageChannelContent();
                    BeanUtils.copyProperties(item, entity);
                    entity.setMessageId(messageId);
                    entity.setContent(fileApi.stripManagedImageSources(entity.getContent()));
                    channelContentMapper.insert(entity);
                });
    }

    private void publish(SysMessage message) {
        message.setStatus(STATUS_SENT);
        message.setPublishTime(LocalDateTime.now());
        updateById(message);
        Runnable sender =
                () ->
                        webSocketMessageSender.sendToAll(
                                WebSocketMessage.toAll(
                                        "system",
                                        Map.of(
                                                "event", "SYSTEM_MESSAGE_PUBLISHED",
                                                "messageId", message.getId(),
                                                "title", message.getTitle())));
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

    private void validateReceiver(
            String receiverType, Integer receiverScope, List<Long> receiverValues) {
        if (!LoginType.ADMIN.equals(receiverType) && !LoginType.USER.equals(receiverType)) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
        if (receiverScope == null
                || receiverScope < RECEIVER_SCOPE_ALL
                || receiverScope > RECEIVER_SCOPE_DEPT) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
        if (receiverScope != RECEIVER_SCOPE_ALL
                && (receiverValues == null || receiverValues.isEmpty())) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
    }

    private void validatePush(Integer pushType, LocalDateTime publishTime) {
        if (pushType == null || pushType < PUSH_TYPE_MANUAL || pushType > PUSH_TYPE_SCHEDULED) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
        if (Integer.valueOf(PUSH_TYPE_SCHEDULED).equals(pushType) && publishTime == null) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
    }

    private int initialStatus(Integer pushType) {
        return Integer.valueOf(PUSH_TYPE_SCHEDULED).equals(pushType)
                ? STATUS_SCHEDULED
                : STATUS_PENDING;
    }

    private String resolveInAppContent(
            List<com.travis.monolith.system.message.api.request.SysMessageChannelContentReq>
                    contents,
            String fallback) {
        if (contents == null) {
            return fallback;
        }
        return contents.stream()
                .filter(item -> "IN_APP".equals(item.getChannel()))
                .map(
                        com.travis.monolith.system.message.api.request.SysMessageChannelContentReq
                                ::getContent)
                .filter(content -> content != null && !content.isBlank())
                .findFirst()
                .orElse(fallback);
    }
}
