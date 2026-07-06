package com.travis.monolith.system.message.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.infrastructure.framework.jackson.core.JsonUtil;
import com.travis.monolith.system.message.api.request.SysMessageChannelContentReq;
import com.travis.monolith.system.message.api.request.SysMessageCreateReq;
import com.travis.monolith.system.message.api.request.SysMessageUpdateReq;
import com.travis.monolith.system.message.api.response.SysMessageChannelContentResp;
import com.travis.monolith.system.message.api.response.SysMessageDetailResp;
import com.travis.monolith.system.message.api.response.SysMessagePageResp;
import com.travis.monolith.system.message.internal.entity.SysMessage;
import com.travis.monolith.system.message.internal.entity.SysMessageChannelContent;
import java.util.Collection;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

/** 消息推送对象转换器。 */
@Mapper(config = BaseMapperConfig.class)
public interface SysMessageConverter {

    @Mapping(target = "receiverValues", qualifiedByName = "serializeReceiverValues")
    SysMessage toEntity(SysMessageCreateReq req);

    @Mapping(target = "receiverValues", qualifiedByName = "serializeReceiverValues")
    void update(SysMessageUpdateReq req, @MappingTarget SysMessage message);

    @Mapping(target = "receiverValues", qualifiedByName = "parseReceiverValues")
    SysMessagePageResp toPageResp(SysMessage message);

    @Mapping(target = "receiverValues", qualifiedByName = "parseReceiverValues")
    SysMessageDetailResp toDetailResp(SysMessage message);

    SysMessageChannelContent toChannelContentEntity(SysMessageChannelContentReq req);

    SysMessageChannelContentResp toChannelContentResp(SysMessageChannelContent entity);

    @Named("serializeReceiverValues")
    default String serializeReceiverValues(Collection<Long> receiverValues) {
        if (receiverValues == null || receiverValues.isEmpty()) {
            return null;
        }
        return JsonUtil.toJsonString(receiverValues.stream().distinct().toList());
    }

    @Named("parseReceiverValues")
    default List<Long> parseReceiverValues(String receiverValues) {
        if (receiverValues == null || receiverValues.isBlank()) {
            return List.of();
        }
        return JsonUtil.parseArray(receiverValues, Long.class);
    }
}
