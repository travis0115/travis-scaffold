package com.travis.monolith.system.message.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.message.api.request.SysMessageTemplateCreateReq;
import com.travis.monolith.system.message.api.request.SysMessageTemplateUpdateReq;
import com.travis.monolith.system.message.api.response.SysMessageTemplateResp;
import com.travis.monolith.system.message.internal.entity.SysMessageTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/** 消息模板对象转换器。 */
@Mapper(config = BaseMapperConfig.class)
public interface SysMessageTemplateConverter {
    /** 将创建参数转换为消息模板实体。 */
    SysMessageTemplate toEntity(SysMessageTemplateCreateReq req);

    /** 将更新参数写入已有消息模板实体。 */
    void update(SysMessageTemplateUpdateReq req, @MappingTarget SysMessageTemplate entity);

    /** 将消息模板实体转换为响应。 */
    SysMessageTemplateResp toResp(SysMessageTemplate entity);
}
