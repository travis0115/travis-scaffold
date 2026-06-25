package com.travis.monolith.system.message.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.message.api.request.SysMessageCreateReq;
import com.travis.monolith.system.message.api.request.SysMessageUpdateReq;
import com.travis.monolith.system.message.api.response.SysMessageDetailResp;
import com.travis.monolith.system.message.api.response.SysMessagePageResp;
import com.travis.monolith.system.message.internal.entity.SysMessage;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(config = BaseMapperConfig.class)
public interface SysMessageConverter {

    @Mapping(target = "targetIds", qualifiedByName = "serializeTargetIds")
    SysMessage toEntity(SysMessageCreateReq req);

    @Mapping(target = "targetIds", qualifiedByName = "serializeTargetIds")
    void update(SysMessageUpdateReq req, @MappingTarget SysMessage message);

    @Mapping(target = "targetIds", qualifiedByName = "parseTargetIds")
    SysMessagePageResp toPageResp(SysMessage message);

    @Mapping(target = "targetIds", qualifiedByName = "parseTargetIds")
    SysMessageDetailResp toDetailResp(SysMessage message);

    @Named("serializeTargetIds")
    default String serializeTargetIds(Collection<Long> targetIds) {
        if (targetIds == null || targetIds.isEmpty()) {
            return null;
        }
        return targetIds.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    @Named("parseTargetIds")
    default List<Long> parseTargetIds(String targetIds) {
        if (targetIds == null || targetIds.isBlank()) {
            return List.of();
        }
        return Arrays.stream(targetIds.split(","))
                .filter(value -> !value.isBlank())
                .map(Long::valueOf)
                .toList();
    }
}
