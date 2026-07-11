package com.travis.monolith.system.message.internal.service.impl;

import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.jackson.core.JsonUtil;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.monolith.system.common.api.BuiltinResourceGuard;
import com.travis.monolith.system.common.api.enums.IsBuiltin;
import com.travis.monolith.system.common.api.enums.SystemErrorCode;
import com.travis.monolith.system.message.api.enums.SysMessageChannel;
import com.travis.monolith.system.message.api.request.SysMessageTemplateCreateReq;
import com.travis.monolith.system.message.api.request.SysMessageTemplatePageReq;
import com.travis.monolith.system.message.api.request.SysMessageTemplateParamConfigReq;
import com.travis.monolith.system.message.api.request.SysMessageTemplateUpdateReq;
import com.travis.monolith.system.message.api.response.SysMessageTemplateResp;
import com.travis.monolith.system.message.internal.converter.SysMessageTemplateConverter;
import com.travis.monolith.system.message.internal.entity.SysMessageTemplate;
import com.travis.monolith.system.message.internal.mapper.SysMessageTemplateMapper;
import com.travis.monolith.system.message.internal.service.SysMessageTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;

import java.util.LinkedHashMap;
import java.util.Map;

/** 消息模板服务实现。 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "system:message-template")
public class SysMessageTemplateServiceImpl
        extends ServiceImplX<SysMessageTemplateMapper, SysMessageTemplate>
        implements SysMessageTemplateService {
    private final SysMessageTemplateConverter converter;
    private final BuiltinResourceGuard builtinResourceGuard;

    @Override
    public PageResp<SysMessageTemplateResp> page(SysMessageTemplatePageReq req) {
        var wrapper =
                new LambdaQueryWrapperX<SysMessageTemplate>()
                        .likeIfPresent(SysMessageTemplate::getTemplateCode, req.getTemplateCode())
                        .likeIfPresent(SysMessageTemplate::getTemplateName, req.getTemplateName())
                        .eqIfPresent(SysMessageTemplate::getChannel, req.getChannel())
                        .eqIfPresent(SysMessageTemplate::getStatus, req.getStatus())
                        .orderByAsc(SysMessageTemplate::getCreateTime);
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
        normalizeContent(req);
        validateUnique(req.getTemplateCode(), req.getChannel(), null);
        save(converter.toEntity(req));
    }

    @Override
    @Transactional
    @CacheEvict(key = "'detail:'+#id")
    public void update(Long id, SysMessageTemplateUpdateReq req) {
        var entity = getByIdOrThrow(id);
        builtinResourceGuard.checkUpdate(entity.getIsBuiltin());
        normalizeContent(req);
        validateUnique(entity.getTemplateCode(), req.getChannel(), id);
        converter.update(req, entity);
        updateById(entity);
    }

    @Override
    @Transactional
    @CacheEvict(key = "'detail:'+#id")
    public void delete(Long id) {
        var entity = getByIdOrThrow(id);
        if (IsBuiltin.YES.getValue().equals(entity.getIsBuiltin())) {
            throw new BizException(SystemErrorCode.MESSAGE_TEMPLATE_BUILTIN_NOT_DELETABLE);
        }
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

    private void normalizeContent(SysMessageTemplateCreateReq req) {
        if (SysMessageChannel.IN_APP.getValue().equals(req.getChannel())) {
            req.setPlatformTemplateId(null);
        } else if (SysMessageChannel.SMS.getValue().equals(req.getChannel())) {
            req.setTitle(null);
        }
        req.setContentSchema(normalizeContentSchema(req.getContentSchema()));
    }

    private void normalizeContent(SysMessageTemplateUpdateReq req) {
        if (SysMessageChannel.IN_APP.getValue().equals(req.getChannel())) {
            req.setPlatformTemplateId(null);
        } else if (SysMessageChannel.SMS.getValue().equals(req.getChannel())) {
            req.setTitle(null);
        }
        req.setContentSchema(normalizeContentSchema(req.getContentSchema()));
    }

    private String normalizeContentSchema(String contentSchema) {
        if (contentSchema == null || contentSchema.isBlank()) {
            return null;
        }
        Map<String, SysMessageTemplateParamConfigReq> schema;
        schema =
                JsonUtil.parseObject(
                        contentSchema,
                        new TypeReference<
                                LinkedHashMap<String, SysMessageTemplateParamConfigReq>>() {});
        if (schema == null || schema.isEmpty()) {
            return null;
        }
        return JsonUtil.toJsonString(schema);
    }
}
