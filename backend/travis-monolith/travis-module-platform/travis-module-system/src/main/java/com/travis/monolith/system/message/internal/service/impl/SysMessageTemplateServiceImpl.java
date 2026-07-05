package com.travis.monolith.system.message.internal.service.impl;

import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.monolith.system.message.api.enums.SysMessageChannel;
import com.travis.monolith.system.message.api.request.SysMessageTemplateCreateReq;
import com.travis.monolith.system.message.api.request.SysMessageTemplatePageReq;
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

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "system:message-template")
public class SysMessageTemplateServiceImpl
        extends ServiceImplX<SysMessageTemplateMapper, SysMessageTemplate>
        implements SysMessageTemplateService {

    private final SysMessageTemplateConverter converter;

    @Override
    public PageResp<SysMessageTemplateResp> page(SysMessageTemplatePageReq req) {
        var wrapper =
                new LambdaQueryWrapperX<SysMessageTemplate>()
                        .likeIfPresent(SysMessageTemplate::getTemplateCode, req.getTemplateCode())
                        .likeIfPresent(SysMessageTemplate::getTemplateName, req.getTemplateName())
                        .eqIfPresent(SysMessageTemplate::getChannel, req.getChannel())
                        .eqIfPresent(SysMessageTemplate::getTemplateType, req.getTemplateType())
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
        validateUnique(req.getTemplateCode(), req.getChannel(), null);
        save(converter.toEntity(req));
    }

    @Override
    @Transactional
    @CacheEvict(key = "'detail:'+#id")
    public void update(Long id, SysMessageTemplateUpdateReq req) {
        var entity = getByIdOrThrow(id);
        validateChannel(req.getChannel());
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
            throw new BizException(CommonErrorCode.BAD_REQUEST, "同通道下模板编码已存在");
        }
    }

    private void validateChannel(String channel) {
        if (!SysMessageChannel.contains(channel)) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "消息通道不支持");
        }
    }
}
