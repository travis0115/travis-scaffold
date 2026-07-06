package com.travis.monolith.system.message.internal.service.impl;

import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.jackson.core.JsonUtil;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.monolith.system.message.api.enums.SysMessageChannel;
import com.travis.monolith.system.message.api.enums.SysMessageTemplateParamType;
import com.travis.monolith.system.message.api.request.SysMessageTemplateCreateReq;
import com.travis.monolith.system.message.api.request.SysMessageTemplatePageReq;
import com.travis.monolith.system.message.api.request.SysMessageTemplateParamConfig;
import com.travis.monolith.system.message.api.request.SysMessageTemplateUpdateReq;
import com.travis.monolith.system.message.api.response.SysMessageTemplateResp;
import com.travis.monolith.system.message.internal.converter.SysMessageTemplateConverter;
import com.travis.monolith.system.message.internal.entity.SysMessageTemplate;
import com.travis.monolith.system.message.internal.mapper.SysMessageTemplateMapper;
import com.travis.monolith.system.message.internal.service.SysMessageTemplateService;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;

/** 消息模板服务实现。 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "system:message-template")
public class SysMessageTemplateServiceImpl
        extends ServiceImplX<SysMessageTemplateMapper, SysMessageTemplate>
        implements SysMessageTemplateService {
    private static final Pattern PARAM_KEY_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z0-9_]*$");
    private static final Pattern TEMPLATE_PARAM_PATTERN =
            Pattern.compile("\\{\\{\\s*([^{}]+?)\\s*}}");

    private final SysMessageTemplateConverter converter;

    @Override
    public PageResp<SysMessageTemplateResp> page(SysMessageTemplatePageReq req) {
        var wrapper =
                new LambdaQueryWrapperX<SysMessageTemplate>()
                        .likeIfPresent(SysMessageTemplate::getTemplateCode, req.getTemplateCode())
                        .likeIfPresent(SysMessageTemplate::getTemplateName, req.getTemplateName())
                        .eqIfPresent(SysMessageTemplate::getChannel, req.getChannel())
                        .eqIfPresent(SysMessageTemplate::getStatus, req.getStatus())
                        .orderByDesc(SysMessageTemplate::getCreateTime);
        return PageConverter.toResp(
                page(req.getPageNum(), req.getPageSize(), wrapper).convert(converter::toResp));
    }

    @Override
    @Cacheable(key = "'detail:'+#id")
    public SysMessageTemplateResp get(Long id) {
        return converter.toResp(getByIdOrThrow(id));
    }

    @Override
    @Transactional
    public void create(SysMessageTemplateCreateReq req) {
        validateChannel(req.getChannel());
        validateAndNormalizeContent(req);
        validateUnique(req.getTemplateCode(), req.getChannel(), null);
        save(converter.toEntity(req));
    }

    @Override
    @Transactional
    @CacheEvict(key = "'detail:'+#id")
    public void update(Long id, SysMessageTemplateUpdateReq req) {
        var entity = getByIdOrThrow(id);
        validateChannel(req.getChannel());
        validateAndNormalizeContent(req);
        validateUnique(req.getTemplateCode(), req.getChannel(), id);
        converter.update(req, entity);
        updateById(entity);
    }

    @Override
    @Transactional
    @CacheEvict(key = "'detail:'+#id")
    public void delete(Long id) {
        removeById(id);
    }

    private void validateUnique(String templateCode, String channel, Long excludeId) {
        var wrapper =
                new LambdaQueryWrapperX<SysMessageTemplate>()
                        .eq(SysMessageTemplate::getTemplateCode, templateCode)
                        .eq(SysMessageTemplate::getChannel, channel)
                        .neIfPresent(SysMessageTemplate::getId, excludeId);
        if (exists(wrapper)) {
            throw new BizException(CommonErrorCode.VALIDATE_FAILED, "同通道下模板编码已存在");
        }
    }

    private void validateChannel(String channel) {
        if (!SysMessageChannel.contains(channel)) {
            throw new BizException(CommonErrorCode.VALIDATE_FAILED, "消息通道不支持");
        }
    }

    private void validateAndNormalizeContent(SysMessageTemplateCreateReq req) {
        if (SysMessageChannel.IN_APP.getValue().equals(req.getChannel())) {
            if (!hasText(req.getTitle())) {
                throw new BizException(CommonErrorCode.VALIDATE_FAILED, "模板标题不能为空");
            }
            if (!hasText(req.getContent())) {
                throw new BizException(CommonErrorCode.VALIDATE_FAILED, "站内信模板内容不能为空");
            }
            req.setPlatformTemplateId(null);
            var schema = validateAndNormalizeContentSchema(req);
            validateTemplateParamUsage(req.getContent(), schema);
            return;
        }
        if (SysMessageChannel.SMS.getValue().equals(req.getChannel())) {
            req.setTitle(null);
        } else if (!hasText(req.getTitle())) {
            throw new BizException(CommonErrorCode.VALIDATE_FAILED, "模板标题不能为空");
        }
        if (!hasText(req.getPlatformTemplateId())) {
            throw new BizException(CommonErrorCode.VALIDATE_FAILED, "平台模板ID不能为空");
        }
        if (!hasText(req.getContent())) {
            throw new BizException(CommonErrorCode.VALIDATE_FAILED, "模板内容不能为空");
        }
        var schema = validateAndNormalizeContentSchema(req);
        validateTemplateParamUsage(req.getContent(), schema);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private Map<String, SysMessageTemplateParamConfig> validateAndNormalizeContentSchema(
            SysMessageTemplateCreateReq req) {
        if (!hasText(req.getContentSchema())) {
            req.setContentSchema(null);
            return Map.of();
        }
        Map<String, SysMessageTemplateParamConfig> schema;
        try {
            schema =
                    JsonUtil.parseObject(
                            req.getContentSchema(),
                            new TypeReference<
                                    LinkedHashMap<String, SysMessageTemplateParamConfig>>() {});
        } catch (RuntimeException ex) {
            throw new BizException(CommonErrorCode.VALIDATE_FAILED, "字段结构必须是合法JSON对象");
        }
        if (schema == null || schema.isEmpty()) {
            req.setContentSchema(null);
            return Map.of();
        }
        schema.forEach(this::validateParamConfig);
        req.setContentSchema(JsonUtil.toJsonString(schema));
        return schema;
    }

    private void validateParamConfig(String key, SysMessageTemplateParamConfig config) {
        if (!hasText(key) || !PARAM_KEY_PATTERN.matcher(key).matches()) {
            throw new BizException(CommonErrorCode.VALIDATE_FAILED, "参数名格式错误");
        }
        if (config == null) {
            throw new BizException(CommonErrorCode.VALIDATE_FAILED, "字段参数配置不能为空");
        }
        if (!hasText(config.getType()) || !SysMessageTemplateParamType.contains(config.getType())) {
            throw new BizException(CommonErrorCode.VALIDATE_FAILED, "字段参数类型不支持");
        }
        if (config.getRequired() == null) {
            throw new BizException(CommonErrorCode.VALIDATE_FAILED, "字段参数必填配置不能为空");
        }
        validateLength(config.getLabel(), 100, "字段参数显示名称长度不能超过100个字符");
        validateLength(config.getDescription(), 255, "字段参数说明长度不能超过255个字符");
    }

    private void validateLength(String value, int max, String message) {
        if (value != null && value.length() > max) {
            throw new BizException(CommonErrorCode.VALIDATE_FAILED, message);
        }
    }

    private void validateTemplateParamUsage(
            String content, Map<String, SysMessageTemplateParamConfig> schema) {
        var contentKeys = extractTemplateParamKeys(content);
        var configuredKeys = schema.keySet();
        var missingKeys =
                contentKeys.stream().filter(key -> !configuredKeys.contains(key)).toList();
        if (!missingKeys.isEmpty()) {
            throw new BizException(
                    CommonErrorCode.VALIDATE_FAILED,
                    "模板内容中的参数未配置：" + String.join("、", missingKeys));
        }
        var extraKeys = configuredKeys.stream().filter(key -> !contentKeys.contains(key)).toList();
        if (!extraKeys.isEmpty()) {
            throw new BizException(
                    CommonErrorCode.VALIDATE_FAILED,
                    "已配置但模板内容未引用的参数：" + String.join("、", extraKeys));
        }
    }

    private LinkedHashSet<String> extractTemplateParamKeys(String content) {
        var keys = new LinkedHashSet<String>();
        if (content == null || content.isBlank()) {
            return keys;
        }
        var matcher = TEMPLATE_PARAM_PATTERN.matcher(content);
        while (matcher.find()) {
            var key = matcher.group(1).trim();
            if (!PARAM_KEY_PATTERN.matcher(key).matches()) {
                throw new BizException(CommonErrorCode.VALIDATE_FAILED, "模板参数引用格式错误：" + key);
            }
            keys.add(key);
        }
        return keys;
    }
}
