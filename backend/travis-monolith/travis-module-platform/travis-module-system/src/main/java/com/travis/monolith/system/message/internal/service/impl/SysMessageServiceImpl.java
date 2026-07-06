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
import com.travis.monolith.system.file.api.SysFileApi;
import com.travis.monolith.system.message.api.enums.SysMessageChannel;
import com.travis.monolith.system.message.api.enums.SysMessageReceiverScope;
import com.travis.monolith.system.message.api.enums.SysMessageSourceType;
import com.travis.monolith.system.message.api.enums.SysMessageTemplateParamType;
import com.travis.monolith.system.message.api.request.SysMessageCreateReq;
import com.travis.monolith.system.message.api.request.SysMessagePageReq;
import com.travis.monolith.system.message.api.request.SysMessageTemplateParamConfig;
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
import com.travis.monolith.system.message.internal.mapper.SysMessageTemplateMapper;
import com.travis.monolith.system.message.internal.service.SysMessageService;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.core.type.TypeReference;

/** 消息推送服务实现。 */
@Service
@CacheConfig(cacheNames = "system:message")
public class SysMessageServiceImpl extends ServiceImplX<SysMessageMapper, SysMessage>
        implements SysMessageService {
    private static final int PUSH_TYPE_MANUAL = 0;
    private static final int PUSH_TYPE_SCHEDULED = 1;
    private static final int STATUS_PENDING = 0;
    private static final int STATUS_SCHEDULED = 1;
    private static final int STATUS_SENT = 2;
    private static final int STATUS_REVOKED = 3;
    private static final Pattern TEMPLATE_VARIABLE_PATTERN =
            Pattern.compile("\\{\\{\\s*([A-Za-z0-9_.-]+)\\s*}}");

    private final SysMessageReceiverMapper messageReceiverMapper;
    private final SysMessageChannelContentMapper channelContentMapper;
    private final SysMessageTemplateMapper messageTemplateMapper;
    private final SysMessageConverter converter;
    private final WebSocketMessageSender webSocketMessageSender;
    private final SysFileApi fileApi;

    public SysMessageServiceImpl(
            SysMessageReceiverMapper messageReceiverMapper,
            SysMessageChannelContentMapper channelContentMapper,
            SysMessageTemplateMapper messageTemplateMapper,
            SysMessageConverter converter,
            WebSocketMessageSender webSocketMessageSender,
            SysFileApi fileApi) {
        this.messageReceiverMapper = messageReceiverMapper;
        this.channelContentMapper = channelContentMapper;
        this.messageTemplateMapper = messageTemplateMapper;
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
        validateChannel(req.getReceiverType(), req.getChannel(), req.getChannelContents());
        validatePush(req.getPushType(), req.getPublishTime());
        var channelContents = renderChannelContents(req.getChannel(), req.getChannelContents());
        var entity = converter.toEntity(req);
        entity.setSourceType(SysMessageSourceType.MANUAL.getValue());
        entity.setEnableInboxCopy(
                normalizeEnableInboxCopy(req.getChannel(), req.getEnableInboxCopy()));
        entity.setStatus(initialStatus(req.getPushType()));
        if (Integer.valueOf(PUSH_TYPE_MANUAL).equals(req.getPushType())) {
            entity.setPublishTime(null);
        }
        entity.setContent(
                fileApi.stripManagedImageSources(
                        resolveInAppContent(channelContents, req.getContent())));
        save(entity);
        syncChannelContents(entity.getId(), channelContents);
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
        validateChannel(req.getReceiverType(), req.getChannel(), req.getChannelContents());
        validatePush(req.getPushType(), req.getPublishTime());
        var channelContents = renderChannelContents(req.getChannel(), req.getChannelContents());
        var entity = getByIdOrThrow(id);
        if (Integer.valueOf(STATUS_SENT).equals(entity.getStatus())) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
        converter.update(req, entity);
        entity.setSourceType(SysMessageSourceType.MANUAL.getValue());
        entity.setEnableInboxCopy(
                normalizeEnableInboxCopy(req.getChannel(), req.getEnableInboxCopy()));
        entity.setStatus(initialStatus(req.getPushType()));
        if (Integer.valueOf(PUSH_TYPE_MANUAL).equals(req.getPushType())) {
            entity.setPublishTime(null);
        }
        entity.setContent(
                fileApi.stripManagedImageSources(
                        resolveInAppContent(channelContents, req.getContent())));
        updateById(entity);
        syncChannelContents(id, channelContents);
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
                            var resp = converter.toChannelContentResp(entity);
                            resp.setContent(fileApi.resolveManagedImageSources(resp.getContent()));
                            return resp;
                        })
                .toList();
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
                    var entity = converter.toChannelContentEntity(item);
                    entity.setMessageId(messageId);
                    entity.setContent(fileApi.stripManagedImageSources(entity.getContent()));
                    channelContentMapper.insert(entity);
                });
    }

    private void publish(SysMessage message) {
        message.setStatus(STATUS_SENT);
        message.setPublishTime(LocalDateTime.now());
        updateById(message);
        if (!isInboxVisible(message)) {
            return;
        }
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

    private void validateChannel(
            String receiverType,
            String channel,
            List<com.travis.monolith.system.message.api.request.SysMessageChannelContentReq>
                    contents) {
        if (channel == null || channel.isBlank()) {
            throw new BizException(CommonErrorCode.VALIDATE_FAILED, "请选择推送通道");
        }
        if (!SysMessageChannel.contains(channel)) {
            throw new BizException(CommonErrorCode.VALIDATE_FAILED, "消息通道不支持");
        }
        if (contents == null) {
            return;
        }
        boolean hasUnsupportedContentChannel =
                contents.stream()
                        .map(
                                com.travis.monolith.system.message.api.request
                                                .SysMessageChannelContentReq
                                        ::getChannel)
                        .filter(Objects::nonNull)
                        .anyMatch(contentChannel -> !channel.equals(contentChannel));
        if (hasUnsupportedContentChannel) {
            throw new BizException(CommonErrorCode.VALIDATE_FAILED, "渠道内容与推送通道不一致");
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

    private boolean normalizeEnableInboxCopy(String channel, Boolean enableInboxCopy) {
        return SysMessageChannel.IN_APP.getValue().equals(channel)
                || Boolean.TRUE.equals(enableInboxCopy);
    }

    private boolean isInboxVisible(SysMessage message) {
        return SysMessageChannel.IN_APP.getValue().equals(message.getChannel())
                || Boolean.TRUE.equals(message.getEnableInboxCopy());
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
                .orElseGet(
                        () ->
                                contents.stream()
                                        .map(
                                                com.travis.monolith.system.message.api.request
                                                                .SysMessageChannelContentReq
                                                        ::getContent)
                                        .filter(content -> content != null && !content.isBlank())
                                        .findFirst()
                                        .orElse(fallback));
    }

    private List<com.travis.monolith.system.message.api.request.SysMessageChannelContentReq>
            renderChannelContents(
                    String channel,
                    List<com.travis.monolith.system.message.api.request.SysMessageChannelContentReq>
                            contents) {
        if (contents == null || contents.isEmpty()) {
            return contents;
        }
        contents.forEach(
                item -> {
                    if (item.getTemplateId() == null) {
                        return;
                    }
                    var template = messageTemplateMapper.selectById(item.getTemplateId());
                    if (template == null || !Objects.equals(template.getChannel(), channel)) {
                        throw new BizException(CommonErrorCode.VALIDATE_FAILED, "消息模板不存在或通道不匹配");
                    }
                    var params = parseTemplateParams(item.getTemplateParams());
                    validateTemplateParams(template.getContentSchema(), params);
                    item.setContent(renderTemplate(template.getContent(), params));
                    item.setTitle(template.getTitle());
                    item.setJumpUrl(template.getRedirectUrl());
                });
        return contents;
    }

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

    private Map<String, SysMessageTemplateParamConfig> parseContentSchema(String contentSchema) {
        if (contentSchema == null || contentSchema.isBlank()) {
            return Map.of();
        }
        try {
            return JsonUtil.parseObject(
                    contentSchema,
                    new TypeReference<LinkedHashMap<String, SysMessageTemplateParamConfig>>() {});
        } catch (RuntimeException ex) {
            throw new BizException(CommonErrorCode.VALIDATE_FAILED, "模板字段结构配置错误");
        }
    }

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
