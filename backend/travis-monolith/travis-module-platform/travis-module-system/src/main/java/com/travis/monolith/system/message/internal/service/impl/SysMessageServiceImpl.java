package com.travis.monolith.system.message.internal.service.impl;

import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.jackson.core.JsonUtil;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.infrastructure.framework.redis.core.annotation.DistributedLock;
import com.travis.infrastructure.framework.redis.core.annotation.DistributedLockNamespace;
import com.travis.monolith.system.common.api.enums.Status;
import com.travis.monolith.system.common.api.enums.SystemErrorCode;
import com.travis.monolith.system.dept.api.SysDeptApi;
import com.travis.monolith.system.file.api.SysFileApi;
import com.travis.monolith.system.message.api.SysMessageReceiverTargetValidator;
import com.travis.monolith.system.message.api.constant.SysMessageTemplatePattern;
import com.travis.monolith.system.message.api.enums.*;
import com.travis.monolith.system.message.api.request.*;
import com.travis.monolith.system.message.api.response.SysMessageResp;
import com.travis.monolith.system.message.internal.channel.SysMessageChannelHandler;
import com.travis.monolith.system.message.internal.channel.SysMessageChannelHandlerRegistry;
import com.travis.monolith.system.message.internal.converter.SysMessageConverter;
import com.travis.monolith.system.message.internal.entity.SysMessage;
import com.travis.monolith.system.message.internal.mapper.SysMessageMapper;
import com.travis.monolith.system.message.internal.model.SysMessageAudience;
import com.travis.monolith.system.message.internal.notification.SysMessageInboxNotifier;
import com.travis.monolith.system.message.internal.quartz.SysMessageScheduleCoordinator;
import com.travis.monolith.system.message.internal.service.SysMessageReceiverService;
import com.travis.monolith.system.message.internal.service.SysMessageService;
import com.travis.monolith.system.message.internal.service.SysMessageTemplateService;
import com.travis.monolith.system.role.api.SysRoleApi;
import com.travis.monolith.system.user.api.SysUserApi;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;
import tools.jackson.core.type.TypeReference;

/** 消息推送服务实现。 */
@Service
@RequiredArgsConstructor
@DistributedLockNamespace("system-message")
public class SysMessageServiceImpl extends ServiceImplX<SysMessageMapper, SysMessage>
        implements SysMessageService {
    private static final String SOURCE_REQUEST_LOCK_KEY_SPEL =
            "#req.sourceType + ':' + #req.sourceId + ':' + #req.receiverType";
    private static final String SOURCE_ARGUMENTS_LOCK_KEY_SPEL =
            "#sourceType + ':' + #sourceId + ':' + #receiverType";
    private static final long SOURCE_LOCK_WAIT_MILLIS = 10_000;

    private final SysMessageReceiverService messageReceiverService;
    private final SysMessageTemplateService messageTemplateService;
    private final SysMessageConverter converter;
    private final SysMessageInboxNotifier inboxNotifier;
    private final SysFileApi fileApi;
    private final SysMessageChannelHandlerRegistry channelHandlerRegistry;
    private final SysMessageScheduleCoordinator scheduleCoordinator;
    private final SysUserApi sysUserApi;
    private final SysRoleApi sysRoleApi;
    private final SysDeptApi sysDeptApi;
    private final ObjectProvider<SysMessageReceiverTargetValidator> receiverTargetValidators;

    /** 分页查询消息。 */
    @Override
    public PageResp<SysMessageResp> page(SysMessagePageReq req) {
        var wrapper = new LambdaQueryWrapperX<SysMessage>();
        wrapper.select(SysMessage.class, field -> !"content".equals(field.getColumn()));
        wrapper.likeIfPresent(SysMessage::getTitle, req.getTitle())
                .eqIfPresent(SysMessage::getMessageType, req.getMessageType())
                .eqIfPresent(SysMessage::getChannel, req.getChannel())
                .eqIfPresent(SysMessage::getReceiverType, req.getReceiverType())
                .eqIfPresent(SysMessage::getPushType, req.getPushType())
                .eqIfPresent(SysMessage::getStatus, req.getStatus())
                .geIfPresent(
                        SysMessage::getPublishTime,
                        req.getPublishStartDate() == null
                                ? null
                                : req.getPublishStartDate().atStartOfDay())
                .ltIfPresent(
                        SysMessage::getPublishTime,
                        req.getPublishEndDate() == null
                                ? null
                                : req.getPublishEndDate().plusDays(1).atStartOfDay())
                .eq(SysMessage::getSourceType, SysMessageSourceType.MANUAL.getValue())
                .ne(SysMessage::getPushType, SysMessagePushType.AUTO.getValue())
                .orderByDesc(SysMessage::getCreateTime);
        var page = page(req.getPageNum(), req.getPageSize(), wrapper);
        page.getRecords().forEach(record -> record.setContent(null));
        return PageConverter.toResp(page.convert(converter::toResp));
    }

    /** 查询指定消息，不存在时抛出业务异常。 */
    @Override
    public SysMessageResp getOrThrow(Long id) {
        var entity = super.getByIdOrThrow(id);
        ensureManualMessage(entity);
        var resp = converter.toResp(entity);
        resp.setContent(fileApi.resolveManagedImageSources(resp.getContent()));
        return resp;
    }

    /** 创建消息。 */
    @Override
    @Transactional
    public Long create(SysMessageCreateReq req) {
        return create(req, SysMessageSourceType.MANUAL);
    }

    /** 创建系统自动消息。 */
    @Override
    @Transactional
    public Long createSystem(SysMessageCreateReq req) {
        return create(req, SysMessageSourceType.SYSTEM);
    }

    /** 按指定独立消息来源创建消息。 */
    private Long create(SysMessageCreateReq req, SysMessageSourceType sourceType) {
        req.setReceiverValues(
                validateReceiver(
                        req.getReceiverType(), req.getReceiverScope(), req.getReceiverValues()));
        validateChannel(req.getChannel());
        validatePush(req.getPushType(), req.getPublishTime());
        var entity = converter.toEntity(req);
        entity.setMessageType(SysMessageType.SYSTEM.getValue());
        entity.setSourceType(sourceType.getValue());
        entity.setSourceId(null);
        entity.setStatus(SysMessageStatus.PENDING.getValue());
        if (SysMessagePushType.MANUAL.getValue().equals(req.getPushType())) {
            entity.setPublishTime(null);
        }
        renderTemplate(entity);
        entity.setContent(fileApi.stripManagedImageSources(entity.getContent()));
        save(entity);
        syncScheduledTriggerAfterCommit(entity);
        return entity.getId();
    }

    /** 更新指定消息。 */
    @Override
    @Transactional
    public void update(Long id, SysMessageUpdateReq req) {
        req.setReceiverValues(
                validateReceiver(
                        req.getReceiverType(), req.getReceiverScope(), req.getReceiverValues()));
        validateChannel(req.getChannel());
        validatePush(req.getPushType(), req.getPublishTime());
        var entity = getByIdForUpdateOrThrow(id);
        ensureManualMessage(entity);
        if (SysMessageStatus.SENT.getValue().equals(entity.getStatus())) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
        converter.update(req, entity);
        entity.setMessageType(SysMessageType.SYSTEM.getValue());
        entity.setSourceType(SysMessageSourceType.MANUAL.getValue());
        entity.setSourceId(null);
        if (!SysMessageStatus.REVOKED.getValue().equals(entity.getStatus())) {
            entity.setStatus(SysMessageStatus.PENDING.getValue());
        }
        if (SysMessagePushType.MANUAL.getValue().equals(req.getPushType())) {
            entity.setPublishTime(null);
        }
        renderTemplate(entity);
        entity.setContent(fileApi.stripManagedImageSources(entity.getContent()));
        updateById(entity);
        syncScheduledTriggerAfterCommit(entity);
    }

    /** 手动推送指定消息。 */
    @Override
    @Transactional
    public void push(Long id) {
        var entity = getByIdForUpdateOrThrow(id);
        ensureManualMessage(entity);
        if (SysMessageStatus.SENT.getValue().equals(entity.getStatus())) {
            return;
        }
        if (!SysMessageStatus.PENDING.getValue().equals(entity.getStatus())
                && !SysMessageStatus.REVOKED.getValue().equals(entity.getStatus())) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
        Integer expectedStatus = entity.getStatus();
        Integer expectedPushType = entity.getPushType();
        LocalDateTime expectedPublishTime = entity.getPublishTime();
        boolean republished = SysMessageStatus.REVOKED.getValue().equals(entity.getStatus());
        entity.setPushType(SysMessagePushType.MANUAL.getValue());
        if (!publish(entity, expectedStatus, expectedPushType, expectedPublishTime)) {
            return;
        }
        syncScheduledTriggerAfterCommit(entity);
        if (republished) {
            resetReceiverReadStatus(entity.getId());
        }
        evictUnreadCache(entity);
    }

    /** 自动推送指定待发送消息。 */
    @Override
    @Transactional
    public void pushAutomatic(Long id) {
        var entity = getByIdForUpdateOrThrow(id);
        ensureSystemMessage(entity);
        if (!SysMessageStatus.PENDING.getValue().equals(entity.getStatus())) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
        Integer expectedPushType = entity.getPushType();
        LocalDateTime expectedPublishTime = entity.getPublishTime();
        entity.setPushType(SysMessagePushType.AUTO.getValue());
        if (!publish(
                entity,
                SysMessageStatus.PENDING.getValue(),
                expectedPushType,
                expectedPublishTime)) {
            return;
        }
        syncScheduledTriggerAfterCommit(entity);
        evictUnreadCache(entity);
    }

    /** 撤回指定站内消息。 */
    @Override
    @Transactional
    public void revoke(Long id) {
        var entity = getByIdForUpdateOrThrow(id);
        ensureManualMessage(entity);
        if (!SysMessageChannel.IN_APP.getValue().equals(entity.getChannel())) {
            throw new BizException(SystemErrorCode.MESSAGE_CHANNEL_REVOKE_NOT_SUPPORTED);
        }
        if (!SysMessageStatus.SENT.getValue().equals(entity.getStatus())) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
        entity.setStatus(SysMessageStatus.REVOKED.getValue());
        updateById(entity);
        evictUnreadCache(entity);
        notifyInboxChanged(SysMessageWebSocketEvent.REVOKED, entity);
    }

    /** 创建或更新业务来源消息，并按发布时间决定是否立即发布。 */
    @Override
    @Transactional
    @DistributedLock(key = SOURCE_REQUEST_LOCK_KEY_SPEL, waitTime = SOURCE_LOCK_WAIT_MILLIS)
    public void publishSourceMessage(SysSourceMessagePublishReq req) {
        req.setReceiverValues(
                validateReceiver(
                        req.getReceiverType(), req.getReceiverScope(), req.getReceiverValues()));
        if (SysMessageSourceType.MANUAL.getValue().equals(req.getSourceType())
                || !isSourceTypeMatched(req.getMessageType(), req.getSourceType())
                || req.getSourceId() == null
                || req.getSourceId().isBlank()
                || req.getTitle() == null
                || req.getTitle().isBlank()
                || req.getPublishTime() == null) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }

        var entity =
                findSourceMessageForUpdate(
                        req.getSourceType(), req.getSourceId(), req.getReceiverType());
        boolean created = entity == null;
        Integer previousStatus = created ? null : entity.getStatus();
        SysMessageAudience previousAudience = created ? null : SysMessageAudience.from(entity);
        boolean republished = SysMessageStatus.REVOKED.getValue().equals(previousStatus);
        if (created) {
            entity = new SysMessage();
            entity.setSourceType(req.getSourceType());
            entity.setSourceId(req.getSourceId());
            entity.setChannel(SysMessageChannel.IN_APP.getValue());
            entity.setContent(null);
        }

        entity.setTitle(req.getTitle());
        entity.setMessageType(req.getMessageType());
        entity.setReceiverType(req.getReceiverType());
        entity.setReceiverScope(req.getReceiverScope());
        entity.setReceiverValues(
                req.getReceiverValues() == null || req.getReceiverValues().isEmpty()
                        ? null
                        : JsonUtil.toJsonString(req.getReceiverValues()));
        entity.setPublishTime(req.getPublishTime());

        boolean due = !req.getPublishTime().isAfter(LocalDateTime.now());
        if (!due) {
            entity.setPushType(SysMessagePushType.SCHEDULED.getValue());
            entity.setStatus(SysMessageStatus.PENDING.getValue());
            saveOrUpdate(entity);
            syncScheduledTriggerAfterCommit(entity);
            if (republished) {
                resetReceiverReadStatus(entity.getId());
            }
            if (SysMessageStatus.SENT.getValue().equals(previousStatus)) {
                evictUnreadCache(previousAudience);
                notifyInboxChanged(
                        SysMessageWebSocketEvent.REVOKED, entity.getId(), previousAudience);
            }
            return;
        }

        entity.setPushType(SysMessagePushType.AUTO.getValue());
        entity.setStatus(SysMessageStatus.SENT.getValue());
        saveOrUpdate(entity);
        syncScheduledTriggerAfterCommit(entity);
        if (republished) {
            resetReceiverReadStatus(entity.getId());
        }
        SysMessageAudience currentAudience = SysMessageAudience.from(entity);
        evictUnreadCache(currentAudience);
        if (SysMessageStatus.SENT.getValue().equals(previousStatus)
                && !currentAudience.equals(previousAudience)) {
            evictUnreadCache(previousAudience);
        }
        if (created || republished || !SysMessageStatus.SENT.getValue().equals(previousStatus)) {
            notifyInboxChanged(
                    republished
                            ? SysMessageWebSocketEvent.REPUBLISHED
                            : SysMessageWebSocketEvent.PUBLISHED,
                    entity);
        } else {
            notifyInboxChanged(SysMessageWebSocketEvent.INBOX_CHANGED, entity, previousAudience);
        }
    }

    /** 撤回指定业务来源消息。 */
    @Override
    @Transactional
    @DistributedLock(key = SOURCE_ARGUMENTS_LOCK_KEY_SPEL, waitTime = SOURCE_LOCK_WAIT_MILLIS)
    public void revokeSourceMessage(String sourceType, String sourceId, String receiverType) {
        var entity = findSourceMessageForUpdate(sourceType, sourceId, receiverType);
        if (entity == null || SysMessageStatus.REVOKED.getValue().equals(entity.getStatus())) {
            return;
        }
        boolean visible = SysMessageStatus.SENT.getValue().equals(entity.getStatus());
        entity.setStatus(SysMessageStatus.REVOKED.getValue());
        updateById(entity);
        syncScheduledTriggerAfterCommit(entity);
        if (visible) {
            evictUnreadCache(entity);
            notifyInboxChanged(SysMessageWebSocketEvent.REVOKED, entity);
        }
    }

    /** 删除指定业务来源消息及其收件状态。 */
    @Override
    @Transactional
    @DistributedLock(key = SOURCE_ARGUMENTS_LOCK_KEY_SPEL, waitTime = SOURCE_LOCK_WAIT_MILLIS)
    public void deleteSourceMessage(String sourceType, String sourceId, String receiverType) {
        var entity = findSourceMessageForUpdate(sourceType, sourceId, receiverType);
        if (entity == null) {
            return;
        }
        boolean visible = SysMessageStatus.SENT.getValue().equals(entity.getStatus());
        messageReceiverService.deleteByMessageId(entity.getId());
        baseMapper.deletePhysicallyById(entity.getId());
        deleteScheduledTriggerAfterCommit(entity.getId());
        if (visible) {
            evictUnreadCache(entity);
            notifyInboxChanged(SysMessageWebSocketEvent.DELETED, entity);
        }
    }

    /** 发布指定的到期定时消息。 */
    @Override
    @Transactional
    public boolean pushScheduled(Long id) {
        var message = baseMapper.selectByIdForUpdate(id);
        if (message == null
                || !SysMessageStatus.PENDING.getValue().equals(message.getStatus())
                || !SysMessagePushType.SCHEDULED.getValue().equals(message.getPushType())
                || message.getPublishTime() == null
                || message.getPublishTime().isAfter(LocalDateTime.now())) {
            return false;
        }
        if (!publish(
                message,
                SysMessageStatus.PENDING.getValue(),
                message.getPushType(),
                message.getPublishTime())) {
            return false;
        }
        evictUnreadCache(message);
        return true;
    }

    /** 删除指定消息。 */
    @Override
    @Transactional
    public void delete(Long id) {
        var entity = getByIdForUpdateOrThrow(id);
        ensureManualMessage(entity);
        boolean visible = SysMessageStatus.SENT.getValue().equals(entity.getStatus());
        messageReceiverService.deleteByMessageId(id);
        removeById(id);
        deleteScheduledTriggerAfterCommit(id);
        if (visible) {
            evictUnreadCache(entity);
            notifyInboxChanged(SysMessageWebSocketEvent.DELETED, entity);
        }
    }

    /** 原子占用待发送消息后执行通道发送，避免手动任务与 Quartz 并发重复推送。 */
    private boolean publish(
            SysMessage message,
            Integer expectedStatus,
            Integer expectedPushType,
            LocalDateTime expectedPublishTime) {
        var channelHandler = getChannelHandler(message.getChannel());
        var publishTime = LocalDateTime.now();
        boolean claimed =
                baseMapper.claimForPublish(
                                message.getId(),
                                expectedStatus,
                                expectedPushType,
                                expectedPublishTime,
                                message.getPushType(),
                                SysMessageStatus.SENT.getValue(),
                                publishTime)
                        > 0;
        if (!claimed) {
            return false;
        }
        message.setStatus(SysMessageStatus.SENT.getValue());
        message.setPublishTime(publishTime);
        channelHandler.send(message);
        notifyInboxChanged(SysMessageWebSocketEvent.PUBLISHED, message);
        return true;
    }

    /** 重置消息接收人的已读状态。 */
    private void resetReceiverReadStatus(Long messageId) {
        messageReceiverService.resetReadStatus(messageId);
    }

    /** 使消息当前接收范围关联的未读数缓存失效。 */
    private void evictUnreadCache(SysMessage message) {
        evictUnreadCache(SysMessageAudience.from(message));
    }

    /** 使接收范围快照关联的未读数缓存失效。 */
    private void evictUnreadCache(SysMessageAudience audience) {
        messageReceiverService.evictUnreadCache(
                audience.receiverType(), audience.receiverScope(), audience.receiverValues());
    }

    /** 事务提交后根据消息当前状态创建、重排或删除一次性任务。 */
    private void syncScheduledTriggerAfterCommit(SysMessage message) {
        syncScheduledTriggerAfterCommit(message.getId());
    }

    /** 事务提交后删除指定消息的一次性任务。 */
    private void deleteScheduledTriggerAfterCommit(Long messageId) {
        syncScheduledTriggerAfterCommit(messageId);
    }

    /** 事务提交后按消息最新数据库状态同步一次性任务。 */
    private void syncScheduledTriggerAfterCommit(Long messageId) {
        scheduleCoordinator.syncAfterCommit(messageId);
    }

    /** 按消息当前及补充接收范围通知收件箱变化。 */
    private void notifyInboxChanged(
            SysMessageWebSocketEvent event,
            SysMessage message,
            SysMessageAudience... additionalAudiences) {
        inboxNotifier.notify(event, message, additionalAudiences);
    }

    /** 按指定接收范围通知收件箱变化。 */
    private void notifyInboxChanged(
            SysMessageWebSocketEvent event, Long messageId, SysMessageAudience... audiences) {
        inboxNotifier.notify(event, messageId, audiences);
    }

    /** 查询并锁定指定消息，不存在时抛出业务异常。 */
    private SysMessage getByIdForUpdateOrThrow(Long id) {
        var message = baseMapper.selectByIdForUpdate(id);
        if (message == null) {
            throw new BizException(SystemErrorCode.MESSAGE_NOT_FOUND);
        }
        return message;
    }

    /** 根据来源信息查询并锁定对应业务消息。 */
    private SysMessage findSourceMessageForUpdate(
            String sourceType, String sourceId, String receiverType) {
        return baseMapper.selectSourceForUpdate(sourceType, sourceId, receiverType);
    }

    /** 校验消息是否允许在消息管理中操作。 */
    private void ensureManualMessage(SysMessage message) {
        if (!SysMessageSourceType.MANUAL.getValue().equals(message.getSourceType())) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "来源消息请在对应业务中操作");
        }
    }

    /** 校验消息是否为系统自动消息。 */
    private void ensureSystemMessage(SysMessage message) {
        if (!SysMessageSourceType.SYSTEM.getValue().equals(message.getSourceType())) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
    }

    /** 判断消息类型与业务来源类型是否匹配。 */
    private boolean isSourceTypeMatched(Integer messageType, String sourceType) {
        return (SysMessageSourceType.NOTICE.getValue().equals(sourceType)
                        && SysMessageType.NOTICE.getValue().equals(messageType))
                || (SysMessageSourceType.VERSION.getValue().equals(sourceType)
                        && SysMessageType.VERSION_UPDATE.getValue().equals(messageType));
    }

    /** 校验消息接收端、接收范围及接收对象。 */
    private List<Long> validateReceiver(
            String receiverType, Integer receiverScope, List<Long> receiverValues) {
        if (!LoginType.ADMIN.equals(receiverType) && !LoginType.APP.equals(receiverType)) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
        if (!SysMessageReceiverScope.contains(receiverScope)) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
        if (LoginType.APP.equals(receiverType)
                && !SysMessageReceiverScope.ALL.getValue().equals(receiverScope)
                && !SysMessageReceiverScope.USER.getValue().equals(receiverScope)) {
            throw new BizException(CommonErrorCode.VALIDATE_FAILED, "客户端用户不支持按角色或部门接收");
        }
        if (SysMessageReceiverScope.ALL.getValue().equals(receiverScope)) {
            return null;
        }
        if (receiverValues == null || receiverValues.isEmpty()) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
        if (receiverValues.size() > 1000) {
            throw new BizException(CommonErrorCode.VALIDATE_FAILED, "接收对象ID不合法或数量超过1000个");
        }
        var normalized = receiverValues.stream().distinct().toList();
        if (normalized.stream().anyMatch(value -> value == null || value <= 0)) {
            throw new BizException(CommonErrorCode.VALIDATE_FAILED, "接收对象ID不合法或数量超过1000个");
        }
        validateReceiverTargets(receiverType, receiverScope, normalized);
        return normalized;
    }

    /** 校验当前模块可查询的接收对象是否真实存在。 */
    private void validateReceiverTargets(
            String receiverType, Integer receiverScope, List<Long> receiverValues) {
        int existingCount;
        try {
            if (LoginType.APP.equals(receiverType)) {
                existingCount =
                        receiverTargetValidators.stream()
                                .filter(
                                        validator ->
                                                receiverType.equals(validator.getReceiverType()))
                                .findFirst()
                                .map(
                                        validator ->
                                                validator
                                                        .findExistingUserIds(receiverValues)
                                                        .size())
                                .orElse(0);
            } else if (SysMessageReceiverScope.USER.getValue().equals(receiverScope)) {
                existingCount = sysUserApi.getUsernameMapByIds(receiverValues).size();
            } else if (SysMessageReceiverScope.ROLE.getValue().equals(receiverScope)) {
                existingCount = sysRoleApi.getRoleNamesByRoleIds(receiverValues).size();
            } else if (SysMessageReceiverScope.DEPT.getValue().equals(receiverScope)) {
                existingCount = sysDeptApi.getDeptNameMapByIds(receiverValues).size();
            } else {
                return;
            }
        } catch (BizException exception) {
            throw new BizException(CommonErrorCode.VALIDATE_FAILED, "接收对象不存在");
        }
        if (existingCount != receiverValues.size()) {
            throw new BizException(CommonErrorCode.VALIDATE_FAILED, "接收对象不存在");
        }
    }

    /** 校验消息通道是否受支持。 */
    private void validateChannel(String channel) {
        getChannelHandler(channel);
    }

    /** 获取消息通道处理器。 */
    private SysMessageChannelHandler getChannelHandler(String channel) {
        if (!SysMessageChannel.contains(channel)) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
        var handler = channelHandlerRegistry.get(channel);
        if (handler == null) {
            throw new BizException(SystemErrorCode.MESSAGE_CHANNEL_UNAVAILABLE);
        }
        return handler;
    }

    /** 校验消息推送方式和定时发布时间。 */
    private void validatePush(Integer pushType, LocalDateTime publishTime) {
        if (pushType == null
                || (!SysMessagePushType.MANUAL.getValue().equals(pushType)
                        && !SysMessagePushType.SCHEDULED.getValue().equals(pushType))) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
        if (SysMessagePushType.SCHEDULED.getValue().equals(pushType) && publishTime == null) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
        if (SysMessagePushType.SCHEDULED.getValue().equals(pushType)
                && !publishTime.isAfter(LocalDateTime.now())) {
            throw new BizException(CommonErrorCode.VALIDATE_FAILED, "定时发布时间必须晚于当前时间");
        }
    }

    /** 使用模板参数渲染消息标题、内容及跳转地址。 */
    private void renderTemplate(SysMessage message) {
        if (!SysMessageChannel.supportsJumpUrl(message.getChannel())) {
            message.setJumpUrl(null);
        }
        if (message.getTemplateId() == null) {
            validateRenderedFields(message);
            validateRenderedJumpUrl(message.getJumpUrl());
            return;
        }
        var template = messageTemplateService.get(message.getTemplateId());
        if (template == null
                || !Status.ENABLED.getValue().equals(template.getStatus())
                || !message.getChannel().equals(template.getChannel())) {
            throw new BizException(CommonErrorCode.VALIDATE_FAILED, "消息模板不存在、已禁用或通道不匹配");
        }
        var params = parseTemplateParams(message.getTemplateParams());
        validateTemplateParams(template.getContentSchema(), params);
        message.setContent(
                renderTemplate(
                        template.getContent(),
                        params,
                        SysMessageChannel.IN_APP.getValue().equals(message.getChannel())));
        message.setTitle(renderTemplate(template.getTitle(), params, false));
        message.setJumpUrl(
                SysMessageChannel.supportsJumpUrl(message.getChannel())
                        ? renderTemplate(template.getRedirectUrl(), params, false)
                        : null);
        if (message.getTitle() == null) {
            message.setTitle("");
        }
        validateRenderedFields(message);
        validateRenderedJumpUrl(message.getJumpUrl());
    }

    /** 校验模板渲染后的字段长度，避免数据库截断或外部通道收到超限内容。 */
    static void validateRenderedFields(SysMessage message) {
        validateRenderedLength(message.getTitle(), 255, "消息标题");
        validateRenderedLength(message.getContent(), 5000, "消息内容");
        validateRenderedLength(message.getJumpUrl(), 500, "跳转地址");
    }

    private static void validateRenderedLength(String value, int maxLength, String fieldName) {
        if (value != null && value.length() > maxLength) {
            throw new BizException(
                    CommonErrorCode.VALIDATE_FAILED, fieldName + "渲染后长度不能超过" + maxLength + "个字符");
        }
    }

    /** 解析并规范化消息模板参数。 */
    private Map<String, Object> parseTemplateParams(String templateParams) {
        if (templateParams == null || templateParams.isBlank()) {
            return Map.of();
        }
        Object value;
        try {
            value = JsonUtil.parseObject(templateParams, Object.class);
        } catch (RuntimeException ex) {
            throw new BizException(CommonErrorCode.VALIDATE_FAILED, "模板参数必须是合法JSON对象");
        }
        if (!(value instanceof Map<?, ?> params)) {
            throw new BizException(CommonErrorCode.VALIDATE_FAILED, "模板参数必须是JSON对象");
        }
        return params.entrySet().stream()
                .collect(
                        java.util.stream.Collectors.toMap(
                                entry -> String.valueOf(entry.getKey()),
                                Map.Entry::getValue,
                                (left, right) -> right,
                                LinkedHashMap::new));
    }

    /** 解析消息模板参数结构。 */
    private Map<String, SysMessageTemplateParamConfigReq> parseContentSchema(String contentSchema) {
        if (contentSchema == null || contentSchema.isBlank()) {
            return Map.of();
        }
        try {
            return JsonUtil.parseObject(
                    contentSchema,
                    new TypeReference<
                            LinkedHashMap<String, SysMessageTemplateParamConfigReq>>() {});
        } catch (RuntimeException ex) {
            throw new BizException(CommonErrorCode.VALIDATE_FAILED, "模板字段结构配置错误");
        }
    }

    /** 根据模板参数结构校验消息模板参数。 */
    private void validateTemplateParams(String contentSchema, Map<String, Object> params) {
        var schema = parseContentSchema(contentSchema);
        schema.forEach(
                (key, config) -> {
                    var value = params.get(key);
                    var text = value == null ? "" : String.valueOf(value).trim();
                    var label =
                            config.getLabel() == null || config.getLabel().isBlank()
                                    ? key
                                    : config.getLabel();
                    if (Boolean.TRUE.equals(config.getRequired()) && text.isBlank()) {
                        throw new BizException(
                                CommonErrorCode.VALIDATE_FAILED, "模板参数【" + label + "】不能为空");
                    }
                    var type = SysMessageTemplateParamType.of(config.getType());
                    if (type == null) {
                        throw new BizException(
                                CommonErrorCode.VALIDATE_FAILED, "模板参数【" + label + "】类型不支持");
                    }
                    if (!type.validate(text)) {
                        throw new BizException(
                                CommonErrorCode.VALIDATE_FAILED,
                                "模板参数【" + label + "】" + type.getInvalidMessage());
                    }
                });
    }

    /** 使用模板参数渲染字段；站内富文本参数按纯文本转义，避免存储型 XSS。 */
    static String renderTemplate(
            String templateContent, Map<String, Object> params, boolean escapeHtml) {
        if (templateContent == null || templateContent.isBlank()) {
            return templateContent;
        }
        var matcher = SysMessageTemplatePattern.TEMPLATE_VARIABLE_PATTERN.matcher(templateContent);
        var result = new StringBuilder();
        while (matcher.find()) {
            var value = params.get(matcher.group(1));
            var replacement = value == null ? "" : String.valueOf(value);
            if (escapeHtml) {
                replacement = HtmlUtils.htmlEscape(replacement, StandardCharsets.UTF_8.name());
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /** 校验模板渲染后的站内路径或 HTTP(S) 地址，防止参数拼出危险协议。 */
    private void validateRenderedJumpUrl(String jumpUrl) {
        if (jumpUrl == null || jumpUrl.isBlank()) {
            return;
        }
        boolean internal =
                jumpUrl.startsWith("/")
                        && !jumpUrl.startsWith("//")
                        && jumpUrl.chars().noneMatch(Character::isWhitespace);
        boolean external =
                ((jumpUrl.startsWith("http://") && jumpUrl.length() > 7)
                                || (jumpUrl.startsWith("https://") && jumpUrl.length() > 8))
                        && jumpUrl.chars().noneMatch(Character::isWhitespace);
        if (!internal && !external) {
            throw new BizException(CommonErrorCode.VALIDATE_FAILED, "模板跳转地址格式不正确");
        }
    }
}
