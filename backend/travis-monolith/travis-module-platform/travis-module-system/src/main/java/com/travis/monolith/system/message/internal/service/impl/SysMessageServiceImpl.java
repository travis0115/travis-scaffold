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
import com.travis.infrastructure.framework.websocket.core.message.WebSocketMessage;
import com.travis.infrastructure.framework.websocket.core.sender.WebSocketMessageSender;
import com.travis.monolith.system.common.api.enums.SystemErrorCode;
import com.travis.monolith.system.file.api.SysFileApi;
import com.travis.monolith.system.message.api.enums.*;
import com.travis.monolith.system.message.api.request.*;
import com.travis.monolith.system.message.api.response.SysMessageResp;
import com.travis.monolith.system.message.internal.converter.SysMessageConverter;
import com.travis.monolith.system.message.internal.entity.SysMessage;
import com.travis.monolith.system.message.internal.mapper.SysMessageMapper;
import com.travis.monolith.system.message.internal.service.SysMessageReceiverService;
import com.travis.monolith.system.message.internal.service.SysMessageService;
import com.travis.monolith.system.message.internal.service.SysMessageTemplateService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
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
import java.util.regex.Pattern;

/** 消息推送服务实现。 */
@Service
@CacheConfig(cacheNames = "system:message")
public class SysMessageServiceImpl extends ServiceImplX<SysMessageMapper, SysMessage>
        implements SysMessageService {
    private static final Pattern TEMPLATE_VARIABLE_PATTERN =
            Pattern.compile("\\{\\{\\s*([A-Za-z0-9_.-]+)\\s*}}");

    private final SysMessageReceiverService messageReceiverService;
    private final SysMessageTemplateService messageTemplateService;
    private final SysMessageConverter converter;
    private final WebSocketMessageSender webSocketMessageSender;
    private final SysFileApi fileApi;

    public SysMessageServiceImpl(
            SysMessageReceiverService messageReceiverService,
            SysMessageTemplateService messageTemplateService,
            SysMessageConverter converter,
            WebSocketMessageSender webSocketMessageSender,
            SysFileApi fileApi) {
        this.messageReceiverService = messageReceiverService;
        this.messageTemplateService = messageTemplateService;
        this.converter = converter;
        this.webSocketMessageSender = webSocketMessageSender;
        this.fileApi = fileApi;
    }

    /** 分页查询消息。 */
    @Override
    public PageResp<SysMessageResp> page(SysMessagePageReq req) {
        var wrapper = new LambdaQueryWrapperX<SysMessage>();
        wrapper.likeIfPresent(SysMessage::getTitle, req.getTitle())
                .eqIfPresent(SysMessage::getPushType, req.getPushType())
                .eqIfPresent(SysMessage::getStatus, req.getStatus())
                .eq(SysMessage::getSourceType, SysMessageSourceType.MANUAL.getValue())
                .ne(SysMessage::getPushType, SysMessagePushType.AUTO.getValue())
                .orderByDesc(SysMessage::getCreateTime);
        Page<SysMessage> page = page(req.getPageNum(), req.getPageSize(), wrapper);
        page.getRecords().forEach(record -> record.setContent(null));
        return PageConverter.toResp(page.convert(converter::toResp));
    }

    /** 查询指定消息，不存在时抛出业务异常。 */
    @Override
    public SysMessageResp getOrThrow(Long id) {
        var resp = converter.toResp(getByIdOrThrow(id));
        resp.setContent(fileApi.resolveManagedImageSources(resp.getContent()));
        return resp;
    }

    /** 创建消息。 */
    @Override
    @Transactional
    @CacheEvict(cacheNames = "system:message:inbox", allEntries = true)
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
        return entity.getId();
    }

    /** 更新指定消息。 */
    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(key = "'detail:'+#id"),
                @CacheEvict(cacheNames = "system:message:inbox", allEntries = true)
            })
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
    }

    /** 手动推送指定消息。 */
    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(key = "'detail:'+#id"),
                @CacheEvict(cacheNames = "system:message:inbox", allEntries = true)
            })
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
        if (republished) {
            resetReceiverReadStatus(entity.getId());
        }
    }

    /** 自动推送指定待发送消息。 */
    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(key = "'detail:'+#id"),
                @CacheEvict(cacheNames = "system:message:inbox", allEntries = true)
            })
    public void pushAutomatic(Long id) {
        var entity = getByIdOrThrow(id);
        ensureManualMessage(entity);
        if (!SysMessageStatus.PENDING.getValue().equals(entity.getStatus())) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
        entity.setPushType(SysMessagePushType.AUTO.getValue());
        publish(entity);
    }

    /** 撤回指定站内消息。 */
    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(key = "'detail:'+#id"),
                @CacheEvict(cacheNames = "system:message:inbox", allEntries = true)
            })
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
        notifyInboxChanged("SYSTEM_MESSAGE_REVOKED", entity);
    }

    /** 创建或更新业务来源消息，并按发布时间决定是否立即发布。 */
    @Override
    @Transactional
    @CacheEvict(cacheNames = "system:message:inbox", allEntries = true)
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
            if (republished) {
                resetReceiverReadStatus(entity.getId());
            }
            if (SysMessageStatus.SENT.getValue().equals(previousStatus)) {
                notifyInboxChanged("SYSTEM_MESSAGE_REVOKED", entity);
            }
            return;
        }

        entity.setPushType(SysMessagePushType.AUTO.getValue());
        entity.setStatus(SysMessageStatus.SENT.getValue());
        saveOrUpdate(entity);
        if (republished) {
            resetReceiverReadStatus(entity.getId());
        }
        if (created || republished || !SysMessageStatus.SENT.getValue().equals(previousStatus)) {
            notifyInboxChanged(
                    republished ? "SYSTEM_MESSAGE_REPUBLISHED" : "SYSTEM_MESSAGE_PUBLISHED",
                    entity);
        } else {
            notifyInboxChanged("SYSTEM_MESSAGE_INBOX_CHANGED", entity);
        }
    }

    /** 撤回指定业务来源消息。 */
    @Override
    @Transactional
    @CacheEvict(cacheNames = "system:message:inbox", allEntries = true)
    public void revokeSourceMessage(String sourceType, String sourceId, String receiverType) {
        var entity = findSourceMessage(sourceType, sourceId, receiverType);
        if (entity == null || SysMessageStatus.REVOKED.getValue().equals(entity.getStatus())) {
            return;
        }
        boolean visible = SysMessageStatus.SENT.getValue().equals(entity.getStatus());
        entity.setStatus(SysMessageStatus.REVOKED.getValue());
        updateById(entity);
        if (visible) {
            notifyInboxChanged("SYSTEM_MESSAGE_REVOKED", entity);
        }
    }

    /** 删除指定业务来源消息及其收件状态。 */
    @Override
    @Transactional
    @CacheEvict(cacheNames = "system:message:inbox", allEntries = true)
    public void deleteSourceMessage(String sourceType, String sourceId, String receiverType) {
        var entity = findSourceMessage(sourceType, sourceId, receiverType);
        if (entity == null) {
            return;
        }
        messageReceiverService.deleteByMessageId(entity.getId());
        removeById(entity.getId());
        notifyInboxChanged("SYSTEM_MESSAGE_DELETED", entity);
    }

    /** 发布所有已到发送时间的定时消息。 */
    @Override
    @Transactional
    @CacheEvict(cacheNames = "system:message:inbox", allEntries = true)
    public int pushDueScheduledMessages() {
        // TODO 后续可改为按消息发布时间创建一次性 Quartz trigger，避免固定频率扫描。
        var messages =
                list(
                        new LambdaQueryWrapperX<SysMessage>()
                                .eq(SysMessage::getStatus, SysMessageStatus.PENDING.getValue())
                                .eq(
                                        SysMessage::getPushType,
                                        SysMessagePushType.SCHEDULED.getValue())
                                .le(SysMessage::getPublishTime, LocalDateTime.now())
                                .orderByAsc(SysMessage::getPublishTime));
        messages.forEach(this::publish);
        return messages.size();
    }

    /** 删除指定消息。 */
    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(key = "'detail:'+#id"),
                @CacheEvict(cacheNames = "system:message:inbox", allEntries = true)
            })
    public void delete(Long id) {
        var entity = getByIdOrThrow(id);
        ensureManualMessage(entity);
        messageReceiverService.deleteByMessageId(id);
        removeById(id);
        notifyInboxChanged("SYSTEM_MESSAGE_DELETED", entity);
    }

    /** 发布消息并更新发布时间与发送状态。 */
    private void publish(SysMessage message) {
        validateChannel(message.getChannel());
        message.setStatus(SysMessageStatus.SENT.getValue());
        message.setPublishTime(LocalDateTime.now());
        updateById(message);
        notifyInboxChanged("SYSTEM_MESSAGE_PUBLISHED", message);
    }

    /** 重置消息接收人的已读状态。 */
    private void resetReceiverReadStatus(Long messageId) {
        messageReceiverService.resetReadStatus(messageId);
    }

    /** 在事务提交后通过 WebSocket 通知收件箱发生变化。 */
    private void notifyInboxChanged(String event, SysMessage message) {
        if (!isInboxVisible(message)) {
            return;
        }
        Runnable sender =
                () ->
                        webSocketMessageSender.sendToAll(
                                WebSocketMessage.toAll(
                                        "system",
                                        Map.of(
                                                "event", event,
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
        if (!SysMessageChannel.contains(channel)) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
        if (!SysMessageChannel.IN_APP.getValue().equals(channel)) {
            throw new BizException(SystemErrorCode.MESSAGE_CHANNEL_UNAVAILABLE);
        }
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
        var matcher = TEMPLATE_VARIABLE_PATTERN.matcher(templateContent);
        var result = new StringBuffer();
        while (matcher.find()) {
            var value = params.get(matcher.group(1));
            matcher.appendReplacement(
                    result, Matcher.quoteReplacement(value == null ? "" : String.valueOf(value)));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
