package com.travis.monolith.system.notice.internal.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.monolith.system.notice.api.request.SysUserMessagePageReq;
import com.travis.monolith.system.notice.api.response.SysUserMessageBaseResp;
import com.travis.monolith.system.notice.api.response.SysUserMessagePageResp;
import com.travis.monolith.system.notice.api.response.SysUserMessageRecentResp;
import com.travis.monolith.system.notice.internal.entity.SysMessage;
import com.travis.monolith.system.notice.internal.entity.SysMessageReceiver;
import com.travis.monolith.system.notice.internal.mapper.SysMessageMapper;
import com.travis.monolith.system.notice.internal.mapper.SysMessageReceiverMapper;
import com.travis.monolith.system.notice.internal.service.SysMessageReceiverService;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SysMessageReceiverServiceImpl
        extends ServiceImplX<SysMessageReceiverMapper, SysMessageReceiver>
        implements SysMessageReceiverService {
    private static final String RECEIVER_TYPE_ADMIN = "ADMIN";

    private final SysMessageMapper messageMapper;

    public SysMessageReceiverServiceImpl(SysMessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    @Override
    public List<SysUserMessageRecentResp> listRecent(Long userId, Integer limit) {
        int actualLimit = limit == null || limit <= 0 ? 10 : Math.min(limit, 50);
        Page<SysMessageReceiver> page =
                page(
                        new Page<>(1, actualLimit),
                        baseWrapper(userId).orderByDesc(SysMessageReceiver::getCreateTime));
        return toResponses(page.getRecords(), SysUserMessageRecentResp::new);
    }

    @Override
    public PageResp<SysUserMessagePageResp> page(Long userId, SysUserMessagePageReq req) {
        var wrapper =
                baseWrapper(userId)
                        .eqIfPresent(SysMessageReceiver::getReadStatus, req.getReadStatus());
        if (req.getTitle() != null && !req.getTitle().isBlank()) {
            List<Long> messageIds =
                    messageMapper
                            .selectList(
                                    new LambdaQueryWrapperX<SysMessage>()
                                            .like(SysMessage::getTitle, req.getTitle()))
                            .stream()
                            .map(SysMessage::getId)
                            .toList();
            if (messageIds.isEmpty()) {
                return PageConverter.toResp(
                        new Page<SysUserMessagePageResp>(req.getPageNum(), req.getPageSize(), 0));
            }
            wrapper.in(SysMessageReceiver::getMessageId, messageIds);
        }
        Page<SysMessageReceiver> page =
                page(
                        new Page<>(req.getPageNum(), req.getPageSize()),
                        wrapper.orderByDesc(SysMessageReceiver::getCreateTime));
        Page<SysUserMessagePageResp> responsePage =
                new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        responsePage.setRecords(toResponses(page.getRecords(), SysUserMessagePageResp::new));
        return PageConverter.toResp(responsePage);
    }

    @Override
    public long countUnread(Long userId) {
        return count(baseWrapper(userId).eq(SysMessageReceiver::getReadStatus, 0));
    }

    @Override
    @Transactional
    public void markRead(Long userId, Long id) {
        boolean updated =
                lambdaUpdate()
                        .eq(SysMessageReceiver::getId, id)
                        .eq(SysMessageReceiver::getReceiverType, RECEIVER_TYPE_ADMIN)
                        .eq(SysMessageReceiver::getReceiverId, userId)
                        .set(SysMessageReceiver::getReadStatus, 1)
                        .set(SysMessageReceiver::getReadTime, LocalDateTime.now())
                        .update();
        if (!updated) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public void markAllRead(Long userId) {
        lambdaUpdate()
                .eq(SysMessageReceiver::getReceiverType, RECEIVER_TYPE_ADMIN)
                .eq(SysMessageReceiver::getReceiverId, userId)
                .eq(SysMessageReceiver::getReadStatus, 0)
                .set(SysMessageReceiver::getReadStatus, 1)
                .set(SysMessageReceiver::getReadTime, LocalDateTime.now())
                .update();
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        baseMapper.deleteMessage(id, RECEIVER_TYPE_ADMIN, userId);
    }

    @Override
    @Transactional
    public void clear(Long userId) {
        baseMapper.deleteByReceiver(RECEIVER_TYPE_ADMIN, userId);
    }

    private LambdaQueryWrapperX<SysMessageReceiver> baseWrapper(Long userId) {
        return new LambdaQueryWrapperX<SysMessageReceiver>()
                .eq(SysMessageReceiver::getReceiverType, RECEIVER_TYPE_ADMIN)
                .eq(SysMessageReceiver::getReceiverId, userId);
    }

    private <T extends SysUserMessageBaseResp> List<T> toResponses(
            List<SysMessageReceiver> messages, Supplier<T> responseFactory) {
        if (messages.isEmpty()) {
            return List.of();
        }
        Collection<Long> messageIds =
                messages.stream().map(SysMessageReceiver::getMessageId).distinct().toList();
        Map<Long, SysMessage> messageMap =
                messageMapper.selectBatchIds(messageIds).stream()
                        .collect(Collectors.toMap(SysMessage::getId, Function.identity()));
        return messages.stream()
                .filter(message -> messageMap.containsKey(message.getMessageId()))
                .map(
                        message ->
                                toResponse(
                                        message,
                                        messageMap.get(message.getMessageId()),
                                        responseFactory.get()))
                .toList();
    }

    private <T extends SysUserMessageBaseResp> T toResponse(
            SysMessageReceiver receiver, SysMessage message, T response) {
        response.setId(receiver.getId());
        response.setMessageId(message.getId());
        response.setTitle(message.getTitle());
        response.setContent(message.getContent());
        response.setMessageType(message.getMessageType());
        response.setReadStatus(receiver.getReadStatus());
        response.setReadTime(receiver.getReadTime());
        response.setPublishTime(message.getPublishTime());
        response.setCreateTime(receiver.getCreateTime());
        return response;
    }
}
