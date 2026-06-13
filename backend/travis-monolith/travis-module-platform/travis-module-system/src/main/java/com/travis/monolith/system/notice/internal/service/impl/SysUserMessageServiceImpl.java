package com.travis.monolith.system.notice.internal.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.monolith.system.notice.api.request.SysUserMessagePageReq;
import com.travis.monolith.system.notice.api.response.SysUserMessageBaseResp;
import com.travis.monolith.system.notice.api.response.SysUserMessagePageResp;
import com.travis.monolith.system.notice.api.response.SysUserMessageRecentResp;
import com.travis.monolith.system.notice.internal.entity.SysNotice;
import com.travis.monolith.system.notice.internal.entity.SysUserMessage;
import com.travis.monolith.system.notice.internal.mapper.SysNoticeMapper;
import com.travis.monolith.system.notice.internal.mapper.SysUserMessageMapper;
import com.travis.monolith.system.notice.internal.service.SysUserMessageService;
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
public class SysUserMessageServiceImpl extends ServiceImpl<SysUserMessageMapper, SysUserMessage>
        implements SysUserMessageService {
    private final SysNoticeMapper noticeMapper;

    public SysUserMessageServiceImpl(SysNoticeMapper noticeMapper) {
        this.noticeMapper = noticeMapper;
    }

    @Override
    public List<SysUserMessageRecentResp> listRecent(Long userId, Integer limit) {
        int actualLimit = limit == null || limit <= 0 ? 10 : Math.min(limit, 50);
        Page<SysUserMessage> page =
                page(
                        new Page<>(1, actualLimit),
                        baseWrapper(userId).orderByDesc(SysUserMessage::getCreateTime));
        return toResponses(page.getRecords(), SysUserMessageRecentResp::new);
    }

    @Override
    public PageResp<SysUserMessagePageResp> page(Long userId, SysUserMessagePageReq req) {
        var wrapper =
                baseWrapper(userId).eqIfPresent(SysUserMessage::getReadStatus, req.getReadStatus());
        if (req.getTitle() != null && !req.getTitle().isBlank()) {
            List<Long> noticeIds =
                    noticeMapper
                            .selectList(
                                    new LambdaQueryWrapperX<SysNotice>()
                                            .like(SysNotice::getTitle, req.getTitle()))
                            .stream()
                            .map(SysNotice::getId)
                            .toList();
            if (noticeIds.isEmpty()) {
                return PageConverter.toResp(
                        new Page<SysUserMessagePageResp>(req.getPageNum(), req.getPageSize(), 0));
            }
            wrapper.in(SysUserMessage::getNoticeId, noticeIds);
        }
        Page<SysUserMessage> page =
                page(
                        new Page<>(req.getPageNum(), req.getPageSize()),
                        wrapper.orderByDesc(SysUserMessage::getCreateTime));
        Page<SysUserMessagePageResp> responsePage =
                new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        responsePage.setRecords(toResponses(page.getRecords(), SysUserMessagePageResp::new));
        return PageConverter.toResp(responsePage);
    }

    @Override
    public long countUnread(Long userId) {
        return count(baseWrapper(userId).eq(SysUserMessage::getReadStatus, 0));
    }

    @Override
    @Transactional
    public void markRead(Long userId, Long id) {
        boolean updated =
                lambdaUpdate()
                        .eq(SysUserMessage::getId, id)
                        .eq(SysUserMessage::getUserId, userId)
                        .set(SysUserMessage::getReadStatus, 1)
                        .set(SysUserMessage::getReadTime, LocalDateTime.now())
                        .update();
        if (!updated) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public void markAllRead(Long userId) {
        lambdaUpdate()
                .eq(SysUserMessage::getUserId, userId)
                .eq(SysUserMessage::getReadStatus, 0)
                .set(SysUserMessage::getReadStatus, 1)
                .set(SysUserMessage::getReadTime, LocalDateTime.now())
                .update();
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        SysUserMessage message =
                getOne(
                        new LambdaQueryWrapperX<SysUserMessage>()
                                .eq(SysUserMessage::getId, id)
                                .eq(SysUserMessage::getUserId, userId));
        if (message == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        baseMapper.deleteMessage(id, userId);
    }

    @Override
    @Transactional
    public void clear(Long userId) {
        baseMapper.deleteByUserId(userId);
    }

    private LambdaQueryWrapperX<SysUserMessage> baseWrapper(Long userId) {
        return new LambdaQueryWrapperX<SysUserMessage>().eq(SysUserMessage::getUserId, userId);
    }

    private <T extends SysUserMessageBaseResp> List<T> toResponses(
            List<SysUserMessage> messages, Supplier<T> responseFactory) {
        if (messages.isEmpty()) {
            return List.of();
        }
        Collection<Long> noticeIds =
                messages.stream().map(SysUserMessage::getNoticeId).distinct().toList();
        Map<Long, SysNotice> noticeMap =
                noticeMapper.selectBatchIds(noticeIds).stream()
                        .collect(Collectors.toMap(SysNotice::getId, Function.identity()));
        return messages.stream()
                .filter(message -> noticeMap.containsKey(message.getNoticeId()))
                .map(
                        message ->
                                toResponse(
                                        message,
                                        noticeMap.get(message.getNoticeId()),
                                        responseFactory.get()))
                .toList();
    }

    private <T extends SysUserMessageBaseResp> T toResponse(
            SysUserMessage message, SysNotice notice, T response) {
        response.setId(message.getId());
        response.setNoticeId(notice.getId());
        response.setTitle(notice.getTitle());
        response.setContent(notice.getContent());
        response.setNoticeType(notice.getNoticeType());
        response.setReadStatus(message.getReadStatus());
        response.setReadTime(message.getReadTime());
        response.setPublishTime(notice.getPublishTime());
        response.setCreateTime(message.getCreateTime());
        return response;
    }
}
