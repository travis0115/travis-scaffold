package com.travis.monolith.system.message.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.infrastructure.framework.jackson.core.JsonUtil;
import com.travis.monolith.system.message.api.request.SysMessageCreateReq;
import com.travis.monolith.system.message.api.request.SysMessageUpdateReq;
import com.travis.monolith.system.message.api.response.SysMessageResp;
import com.travis.monolith.system.message.internal.entity.SysMessage;
import java.util.Collection;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

/** 消息推送对象转换器。 */
@Mapper(config = BaseMapperConfig.class)
public interface SysMessageConverter {

    /** 将创建参数转换为消息实体。 */
    @Mapping(target = "receiverValues", qualifiedByName = "serializeReceiverValues")
    SysMessage toEntity(SysMessageCreateReq req);

    /** 将更新参数写入已有消息实体。 */
    @Mapping(target = "receiverValues", qualifiedByName = "serializeReceiverValues")
    void update(SysMessageUpdateReq req, @MappingTarget SysMessage message);

    /** 将消息实体转换为响应。 */
    @Mapping(target = "receiverValues", qualifiedByName = "parseReceiverValues")
    SysMessageResp toResp(SysMessage message);

    /** 将接收对象 ID 去重后序列化为 JSON 数组。 */
    @Named("serializeReceiverValues")
    default String serializeReceiverValues(Collection<Long> receiverValues) {
        if (receiverValues == null || receiverValues.isEmpty()) {
            return null;
        }
        return JsonUtil.toJsonString(receiverValues.stream().distinct().toList());
    }

    /** 将 JSON 数组格式的接收对象 ID 解析为列表。 */
    @Named("parseReceiverValues")
    default List<Long> parseReceiverValues(String receiverValues) {
        if (receiverValues == null || receiverValues.isBlank()) {
            return List.of();
        }
        return JsonUtil.parseArray(receiverValues, Long.class);
    }
}
