package com.travis.monolith.system.message.internal.service.impl;

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
import com.travis.monolith.system.common.api.enums.SystemErrorCode;
import com.travis.monolith.system.file.api.SysFileApi;
import com.travis.monolith.system.message.api.constant.SysMessageTemplatePattern;
import com.travis.monolith.system.message.api.enums.*;
import com.travis.monolith.system.message.api.request.*;
import com.travis.monolith.system.message.api.response.SysMessageResp;
import com.travis.monolith.system.message.internal.channel.SysMessageChannelHandler;
import com.travis.monolith.system.message.internal.channel.SysMessageChannelHandlerRegistry;
import com.travis.monolith.system.message.internal.converter.SysMessageConverter;
import com.travis.monolith.system.message.internal.entity.SysMessage;
import com.travis.monolith.system.message.internal.mapper.SysMessageMapper;
import com.travis.monolith.system.message.internal.quartz.SysMessageScheduledPushScheduler;
import com.travis.monolith.system.message.internal.service.SysMessageReceiverService;
import com.travis.monolith.system.message.internal.service.SysMessageService;
import com.travis.monolith.system.message.internal.service.SysMessageTemplateService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.core.type.TypeReference;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/** 消息推送服务实现。 */
@Service
@CacheConfig(cacheNames = "system:message")
@AllArgsConstructor
@Slf4j
public class SysMessageServiceImpl extends ServiceImplX<SysMessageMapper, SysMessage>
        implements SysMessageService {
    private final SysMessageReceiverService messageReceiverService;
    private final SysMessageTemplateService messageTemplateService;
    private final SysMessageConverter converter;
    private final WebSocketMessageSender webSocketMessageSender;
    private final SysFileApi fileApi;
    private final CacheManager cacheManager;
    private final SysMessageChannelHandlerRegistry channelHandlerRegistry;
    private final SysMessageScheduledPushScheduler scheduledPushScheduler;

    /** 分页查询消息。 */
    @Override
    public PageResp<SysMessageResp> page(SysMessagePageReq req) {
        var wrapper = new LambdaQueryWrapperX<SysMessage>();
        wrapper.likeIfPresent(SysMessage::getTitle, req.getTitle())
                .eqIfPresent(SysMessage::getMessageType, req.getMessageType())
                .eqIfPresent(SysMessage::getChannel, req.getChannel())
                .eqIfPresent(SysMessage::getReceiverType, req.getReceiverType())
                .eqIfPresent(SysMessage::getPushType, req.getPushType())
                .eqIfPresent(SysMessage::getStatus, req.getStatus())
                .eq(SysMessage::getSourceType, SysMessageSourceType.MANUAL.getValue())
                .ne(SysMessage::getPushType, SysMessagePushType.AUTO.getValue())
                .orderByDesc(SysMessage::getCreateTime);
        var page = page(req.getPageNum(), req.getPageSize(), wrapper);
        page.getRecords().forEach(record -> record.setContent(null));
        return PageConverter.toResp(page.convert(converter::toResp));
    }

    /** 查询指定消息，不存在时抛出业务异常。 */
    @Override
    @Cacheable(key = "'detail:'+#id")
    public SysMessageResp getOrThrow(Long id) {
        var resp = converter.toResp(super.getByIdOrThrow(id));
        resp.setContent(fileApi.resolveManagedImageSources(resp.getContent()));
        return resp;
    }

    /** 创建消息。 */
    @Override
    @Transactional
    public Long create(SysMessageCreateReq req) {
        validateReceiver(req.getReceiverType(), req.getReceiverScope(), req.getReceiverValues());
        validateChannel(req.getChannel());
        validatePush(req.getPushType(), req.getPublishTime());
        var entity = converter.toEntity(req);
        entity.setMessageType(SysMessageType.SYSTEM.getValue());
        entity.setSourceType(SysMessageSourceType.MANUAL.getValue());
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
    @CacheEvict(key = "'detail:'+#id")
    public void update(Long id, SysMessageUpdateReq req) {
        validateReceiver(req.getReceiverType(), req.getReceiverScope(), req.getReceiverValues());
        validateChannel(req.getChannel());
        validatePush(req.getPushType(), req.getPublishTime());
        var entity = getByIdOrThrow(id);
        ensureManualMessage(entity);
        if (SysMessageStatus.SENT.getValue().equals(entity.getStatus())) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
        converter.update(req, entity);
        entity.setMessageType(SysMessageType.SYSTEM.getValue());
        entity.setSourceType(SysMessageSourceType.MANUAL.getValue());
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
    @CacheEvict(key = "'detail:'+#id")
    public void push(Long id) {
        var entity = getByIdOrThrow(id);
        ensureManualMessage(entity);
        if (SysMessageStatus.SENT.getValue().equals(entity.getStatus())) {
            return;
        }
        if (!SysMessageStatus.PENDING.getValue().equals(entity.getStatus())
                && !SysMessageStatus.REVOKED.getValue().equals(entity.getStatus())) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
        boolean republished = SysMessageStatus.REVOKED.getValue().equals(entity.getStatus());
        entity.setPushType(SysMessagePushType.MANUAL.getValue());
        publish(entity);
        syncScheduledTriggerAfterCommit(entity);
        if (republished) {
            resetReceiverReadStatus(entity.getId());
        }
        messageReceiverService.evictUnreadCache();
    }

    /** 自动推送指定待发送消息。 */
    @Override
    @Transactional
    @CacheEvict(key = "'detail:'+#id")
    public void pushAutomatic(Long id) {
        var entity = getByIdOrThrow(id);
        ensureManualMessage(entity);
        if (!SysMessageStatus.PENDING.getValue().equals(entity.getStatus())) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
        entity.setPushType(SysMessagePushType.AUTO.getValue());
        publish(entity);
        syncScheduledTriggerAfterCommit(entity);
        messageReceiverService.evictUnreadCache();
    }

    /** 撤回指定站内消息。 */
    @Override
    @Transactional
    @CacheEvict(key = "'detail:'+#id")
    public void revoke(Long id) {
        var entity = getByIdOrThrow(id);
        ensureManualMessage(entity);
        if (!SysMessageChannel.IN_APP.getValue().equals(entity.getChannel())) {
            throw new BizException(SystemErrorCode.MESSAGE_CHANNEL_REVOKE_NOT_SUPPORTED);
        }
        if (!SysMessageStatus.SENT.getValue().equals(entity.getStatus())) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
        entity.setStatus(SysMessageStatus.REVOKED.getValue());
        updateById(entity);
        messageReceiverService.evictUnreadCache();
        notifyInboxChanged(SysMessageWebSocketEvent.REVOKED, entity);
    }

    /** 创建或更新业务来源消息，并按发布时间决定是否立即发布。 */
    @Override
    @Transactional
    public void publishSourceMessage(SysSourceMessagePublishReq req) {
        validateReceiver(req.getReceiverType(), req.getReceiverScope(), req.getReceiverValues());
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
                findSourceMessage(req.getSourceType(), req.getSourceId(), req.getReceiverType());
        boolean created = entity == null;
        Integer previousStatus = created ? null : entity.getStatus();
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
                req.getReceiverValues() == null
                        ? null
                        : JsonUtil.toJsonString(req.getReceiverValues()));
        entity.setPublishTime(req.getPublishTime());

        boolean due = !req.getPublishTime().isAfter(LocalDateTime.now());
        if (!due) {
            entity.setPushType(SysMessagePushType.SCHEDULED.getValue());
            entity.setStatus(SysMessageStatus.PENDING.getValue());
            saveOrUpdate(entity);
            syncScheduledTriggerAfterCommit(entity);
            evictDetailCache(entity.getId());
            if (republished) {
                resetReceiverReadStatus(entity.getId());
            }
            if (SysMessageStatus.SENT.getValue().equals(previousStatus)) {
                messageReceiverService.evictUnreadCache();
                notifyInboxChanged(SysMessageWebSocketEvent.REVOKED, entity);
            }
            return;
        }

        entity.setPushType(SysMessagePushType.AUTO.getValue());
        entity.setStatus(SysMessageStatus.SENT.getValue());
        saveOrUpdate(entity);
        syncScheduledTriggerAfterCommit(entity);
        evictDetailCache(entity.getId());
        if (republished) {
            resetReceiverReadStatus(entity.getId());
        }
        messageReceiverService.evictUnreadCache();
        if (created || republished || !SysMessageStatus.SENT.getValue().equals(previousStatus)) {
            notifyInboxChanged(
                    republished
                            ? SysMessageWebSocketEvent.REPUBLISHED
                            : SysMessageWebSocketEvent.PUBLISHED,
                    entity);
        } else {
            notifyInboxChanged(SysMessageWebSocketEvent.INBOX_CHANGED, entity);
        }
    }

    /** 撤回指定业务来源消息。 */
    @Override
    @Transactional
    public void revokeSourceMessage(String sourceType, String sourceId, String receiverType) {
        var entity = findSourceMessage(sourceType, sourceId, receiverType);
        if (entity == null || SysMessageStatus.REVOKED.getValue().equals(entity.getStatus())) {
            return;
        }
        boolean visible = SysMessageStatus.SENT.getValue().equals(entity.getStatus());
        entity.setStatus(SysMessageStatus.REVOKED.getValue());
        updateById(entity);
        syncScheduledTriggerAfterCommit(entity);
        evictDetailCache(entity.getId());
        if (visible) {
            messageReceiverService.evictUnreadCache();
            notifyInboxChanged(SysMessageWebSocketEvent.REVOKED, entity);
        }
    }

    /** 删除指定业务来源消息及其收件状态。 */
    @Override
    @Transactional
    public void deleteSourceMessage(String sourceType, String sourceId, String receiverType) {
        var entity = findSourceMessage(sourceType, sourceId, receiverType);
        if (entity == null) {
            return;
        }
        boolean visible = SysMessageStatus.SENT.getValue().equals(entity.getStatus());
        messageReceiverService.deleteByMessageId(entity.getId());
        removeById(entity.getId());
        deleteScheduledTriggerAfterCommit(entity.getId());
        evictDetailCache(entity.getId());
        if (visible) {
            messageReceiverService.evictUnreadCache();
        }
        notifyInboxChanged(SysMessageWebSocketEvent.DELETED, entity);
    }

    /** 发布指定的到期定时消息。 */
    @Override
    @Transactional
    public boolean pushScheduled(Long id) {
        var message = getById(id);
        if (message == null
                || !SysMessageStatus.PENDING.getValue().equals(message.getStatus())
                || !SysMessagePushType.SCHEDULED.getValue().equals(message.getPushType())
                || message.getPublishTime() == null
                || message.getPublishTime().isAfter(LocalDateTime.now())) {
            return false;
        }
        publish(message);
        evictDetailCache(message.getId());
        messageReceiverService.evictUnreadCache();
        return true;
    }

    /** 删除指定消息。 */
    @Override
    @Transactional
    @CacheEvict(key = "'detail:'+#id")
    public void delete(Long id) {
        var entity = getByIdOrThrow(id);
        ensureManualMessage(entity);
        boolean visible = SysMessageStatus.SENT.getValue().equals(entity.getStatus());
        messageReceiverService.deleteByMessageId(id);
        removeById(id);
        deleteScheduledTriggerAfterCommit(id);
        if (visible) {
            messageReceiverService.evictUnreadCache();
        }
        notifyInboxChanged(SysMessageWebSocketEvent.DELETED, entity);
    }

    /** 发布消息并更新发布时间与发送状态。 */
    private void publish(SysMessage message) {
        var channelHandler = getChannelHandler(message.getChannel());
        channelHandler.send(message);
        message.setStatus(SysMessageStatus.SENT.getValue());
        message.setPublishTime(LocalDateTime.now());
        updateById(message);
        notifyInboxChanged(SysMessageWebSocketEvent.PUBLISHED, message);
    }

    /** 重置消息接收人的已读状态。 */
    private void resetReceiverReadStatus(Long messageId) {
        messageReceiverService.resetReadStatus(messageId);
    }

    /** 精准清除指定消息详情缓存。 */
    private void evictDetailCache(Long messageId) {
        var cache = cacheManager.getCache("system:message");
        if (cache != null) {
            cache.evict("detail:" + messageId);
        }
    }

    /** 事务提交后根据消息当前状态创建、重排或删除一次性任务。 */
    private void syncScheduledTriggerAfterCommit(SysMessage message) {
        Long messageId = message.getId();
        LocalDateTime publishTime = message.getPublishTime();
        boolean scheduled =
                SysMessageStatus.PENDING.getValue().equals(message.getStatus())
                        && SysMessagePushType.SCHEDULED.getValue().equals(message.getPushType())
                        && publishTime != null;
        runAfterCommit(
                messageId,
                scheduled
                        ? () -> scheduledPushScheduler.schedule(messageId, publishTime)
                        : () -> scheduledPushScheduler.delete(messageId));
    }

    /** 事务提交后删除指定消息的一次性任务。 */
    private void deleteScheduledTriggerAfterCommit(Long messageId) {
        runAfterCommit(messageId, () -> scheduledPushScheduler.delete(messageId));
    }

    /** 在事务确认提交后同步 Quartz，失败时保留业务数据供下次启动对账。 */
    private void runAfterCommit(Long messageId, Runnable action) {
        Runnable guardedAction =
                () -> {
                    try {
                        action.run();
                    } catch (Exception exception) {
                        log.error("[消息调度] 同步一次性任务失败，messageId={}", messageId, exception);
                    }
                };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCompletion(int status) {
                            if (status == TransactionSynchronization.STATUS_COMMITTED) {
                                guardedAction.run();
                            }
                        }
                    });
        } else {
            guardedAction.run();
        }
    }

    /** 在事务完成且确认提交后通过 WebSocket 通知收件箱发生变化。 */
    private void notifyInboxChanged(SysMessageWebSocketEvent event, SysMessage message) {
        if (!isInboxVisible(message)) {
            return;
        }
        Runnable sender =
                () ->
                        webSocketMessageSender.sendToAll(
                                WebSocketMessage.toAll(
                                        WebSocketSender.SYSTEM,
                                        event,
                                        Map.of(
                                                "messageId", message.getId(),
                                                "title", message.getTitle())));
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

    /** 根据来源信息查询对应业务消息。 */
    private SysMessage findSourceMessage(String sourceType, String sourceId, String receiverType) {
        return getOne(
                new LambdaQueryWrapperX<SysMessage>()
                        .eq(SysMessage::getSourceType, sourceType)
                        .eq(SysMessage::getSourceId, sourceId)
                        .eq(SysMessage::getReceiverType, receiverType));
    }

    /** 校验消息是否允许在消息管理中操作。 */
    private void ensureManualMessage(SysMessage message) {
        if (!SysMessageSourceType.MANUAL.getValue().equals(message.getSourceType())) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "来源消息请在对应业务中操作");
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
    private void validateReceiver(
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
        if (!SysMessageReceiverScope.ALL.getValue().equals(receiverScope)
                && (receiverValues == null || receiverValues.isEmpty())) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
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
    }

    /** 判断消息是否应出现在站内收件箱。 */
    private boolean isInboxVisible(SysMessage message) {
        return SysMessageChannel.IN_APP.getValue().equals(message.getChannel());
    }

    /** 使用模板参数渲染消息标题、内容及跳转地址。 */
    private void renderTemplate(SysMessage message) {
        if (message.getTemplateId() == null) {
            return;
        }
        var template = messageTemplateService.get(message.getTemplateId());
        if (template == null || !message.getChannel().equals(template.getChannel())) {
            throw new BizException(CommonErrorCode.VALIDATE_FAILED, "消息模板不存在或通道不匹配");
        }
        var params = parseTemplateParams(message.getTemplateParams());
        validateTemplateParams(template.getContentSchema(), params);
        message.setContent(renderTemplate(template.getContent(), params));
        message.setTitle(template.getTitle());
        message.setJumpUrl(template.getRedirectUrl());
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

    /** 使用模板参数渲染消息标题、内容及跳转地址。 */
    private String renderTemplate(String templateContent, Map<String, Object> params) {
        if (templateContent == null || templateContent.isBlank()) {
            return templateContent;
        }
        var matcher = SysMessageTemplatePattern.TEMPLATE_VARIABLE_PATTERN.matcher(templateContent);
        var result = new StringBuilder();
        while (matcher.find()) {
            var value = params.get(matcher.group(1));
            matcher.appendReplacement(
                    result, Matcher.quoteReplacement(value == null ? "" : String.valueOf(value)));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
