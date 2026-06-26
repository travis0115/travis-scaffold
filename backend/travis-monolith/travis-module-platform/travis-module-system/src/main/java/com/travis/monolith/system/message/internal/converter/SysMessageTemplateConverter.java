package com.travis.monolith.system.message.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.message.api.request.SysMessageTemplateCreateReq;
import com.travis.monolith.system.message.api.request.SysMessageTemplateUpdateReq;
import com.travis.monolith.system.message.api.response.SysMessageTemplateResp;
import com.travis.monolith.system.message.internal.entity.SysMessageTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = BaseMapperConfig.class)
public interface SysMessageTemplateConverter {
    SysMessageTemplate toEntity(SysMessageTemplateCreateReq req);

    void update(SysMessageTemplateUpdateReq req, @MappingTarget SysMessageTemplate entity);

    SysMessageTemplateResp toResp(SysMessageTemplate entity);
}
