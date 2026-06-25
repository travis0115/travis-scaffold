package com.travis.monolith.system.notice.internal.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.monolith.system.notice.api.request.SysMessageCreateReq;
import com.travis.monolith.system.notice.api.request.SysMessagePageReq;
import com.travis.monolith.system.notice.api.request.SysMessageUpdateReq;
import com.travis.monolith.system.notice.api.response.SysMessageDetailResp;
import com.travis.monolith.system.notice.api.response.SysMessagePageResp;
import com.travis.monolith.system.notice.internal.converter.SysMessageConverter;
import com.travis.monolith.system.notice.internal.entity.SysMessage;
import com.travis.monolith.system.notice.internal.entity.SysMessageReceiver;
import com.travis.monolith.system.notice.internal.mapper.SysMessageMapper;
import com.travis.monolith.system.notice.internal.mapper.SysMessageReceiverMapper;
import com.travis.monolith.system.notice.internal.service.SysMessageService;
import com.travis.monolith.system.role.api.SysRoleApi;
import com.travis.monolith.system.user.api.SysUserApi;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@CacheConfig(cacheNames = "system:message")
public class SysMessageServiceImpl extends ServiceImplX<SysMessageMapper, SysMessage>
        implements SysMessageService {
    private static final String RECEIVER_TYPE_ADMIN = "ADMIN";
    private static final int AUDIENCE_ALL = 0;
    private static final int AUDIENCE_USER = 1;
    private static final int AUDIENCE_ROLE = 2;
    private static final int AUDIENCE_DEPT = 3;

    private final SysMessageReceiverMapper messageReceiverMapper;
    private final SysUserApi userApi;
    private final SysRoleApi roleApi;
    private final SysMessageConverter converter;

    public SysMessageServiceImpl(
            SysMessageReceiverMapper messageReceiverMapper,
            SysUserApi userApi,
            SysRoleApi roleApi,
            SysMessageConverter converter) {
        this.messageReceiverMapper = messageReceiverMapper;
        this.userApi = userApi;
        this.roleApi = roleApi;
        this.converter = converter;
    }

    @Override
    public PageResp<SysMessagePageResp> page(SysMessagePageReq req) {
        var wrapper =
                new LambdaQueryWrapperX<SysMessage>()
                        .likeIfPresent(SysMessage::getTitle, req.getTitle())
                        .eqIfPresent(SysMessage::getMessageType, req.getMessageType())
                        .eqIfPresent(SysMessage::getStatus, req.getStatus())
                        .orderByDesc(SysMessage::getCreateTime);
        Page<SysMessage> page = page(req.getPageNum(), req.getPageSize(), wrapper);
        return PageConverter.toResp(page.convert(this::toPageResp));
    }

    @Override
    @Cacheable(key = "'detail:'+#id")
    public SysMessageDetailResp get(Long id) {
        return toDetailResp(getByIdOrThrow(id));
    }

    @Override
    @Transactional
    public void create(SysMessageCreateReq req) {
        validateAudience(req.getAudienceType(), req.getTargetIds());
        var entity = converter.toEntity(req);
        save(entity);
        if (Integer.valueOf(1).equals(entity.getStatus())) {
            publish(entity);
        }
    }

    @Override
    @Transactional
    @CacheEvict(key = "'detail:'+#id")
    public void updateStatus(Long id, Integer status) {
        var entity = getByIdOrThrow(id);
        entity.setStatus(status);
        updateById(entity);
        if (Integer.valueOf(1).equals(entity.getStatus())) {
            publish(entity);
        }
    }

    @Override
    @Transactional
    @CacheEvict(key = "'detail:'+#id")
    public void update(Long id, SysMessageUpdateReq req) {
        validateAudience(req.getAudienceType(), req.getTargetIds());
        var entity = getByIdOrThrow(id);
        converter.update(req, entity);
        updateById(entity);
        if (Integer.valueOf(1).equals(entity.getStatus())) {
            publish(entity);
        }
    }

    @Override
    @Transactional
    @CacheEvict(key = "'detail:'+#id")
    public void delete(Long id) {
        messageReceiverMapper.deleteByMessageId(id);
        removeById(id);
    }

    private void publish(SysMessage message) {
        if (message.getPublishTime() == null) {
            message.setPublishTime(LocalDateTime.now());
            updateById(message);
        }
        Set<Long> recipientIds = new LinkedHashSet<>(resolveRecipients(message));
        if (recipientIds.isEmpty()) {
            return;
        }
        Set<Long> existingUserIds =
                messageReceiverMapper
                        .selectList(
                                new LambdaQueryWrapperX<SysMessageReceiver>()
                                        .eq(SysMessageReceiver::getMessageId, message.getId())
                                        .eq(
                                                SysMessageReceiver::getReceiverType,
                                                RECEIVER_TYPE_ADMIN)
                                        .in(SysMessageReceiver::getReceiverId, recipientIds))
                        .stream()
                        .map(SysMessageReceiver::getReceiverId)
                        .collect(Collectors.toSet());
        recipientIds.stream()
                .filter(userId -> !existingUserIds.contains(userId))
                .forEach(
                        userId -> {
                            var receiver = new SysMessageReceiver();
                            receiver.setMessageId(message.getId());
                            receiver.setReceiverType(RECEIVER_TYPE_ADMIN);
                            receiver.setReceiverId(userId);
                            receiver.setReadStatus(0);
                            messageReceiverMapper.insert(receiver);
                        });
    }

    private List<Long> resolveRecipients(SysMessage message) {
        List<Long> targetIds = parseTargetIds(message.getTargetIds());
        return switch (message.getAudienceType()) {
            case AUDIENCE_ALL -> userApi.listUserIds();
            case AUDIENCE_USER -> targetIds;
            case AUDIENCE_ROLE -> roleApi.getUserIdsByRoleIds(targetIds);
            case AUDIENCE_DEPT -> userApi.listUserIdsByDeptIds(targetIds);
            default -> throw new BizException(CommonErrorCode.BAD_REQUEST);
        };
    }

    private void validateAudience(Integer audienceType, List<Long> targetIds) {
        if (audienceType == null || audienceType < AUDIENCE_ALL || audienceType > AUDIENCE_DEPT) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
        if (audienceType != AUDIENCE_ALL && (targetIds == null || targetIds.isEmpty())) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
    }

    private SysMessagePageResp toPageResp(SysMessage message) {
        var response = new SysMessagePageResp();
        BeanUtils.copyProperties(message, response, "targetIds");
        response.setTargetIds(parseTargetIds(message.getTargetIds()));
        return response;
    }

    private SysMessageDetailResp toDetailResp(SysMessage message) {
        var response = new SysMessageDetailResp();
        BeanUtils.copyProperties(message, response, "targetIds");
        response.setTargetIds(parseTargetIds(message.getTargetIds()));
        return response;
    }

    private List<Long> parseTargetIds(String targetIds) {
        if (targetIds == null || targetIds.isBlank()) {
            return List.of();
        }
        return Arrays.stream(targetIds.split(","))
                .filter(value -> !value.isBlank())
                .map(Long::valueOf)
                .toList();
    }
}
