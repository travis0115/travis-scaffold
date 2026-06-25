package com.travis.monolith.system.message.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.message.api.request.SysMessageCreateReq;
import com.travis.monolith.system.message.api.request.SysMessageUpdateReq;
import com.travis.monolith.system.message.internal.entity.SysMessage;
import java.util.Collection;
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

    @Named("serializeTargetIds")
    default String serializeTargetIds(Collection<Long> targetIds) {
        if (targetIds == null || targetIds.isEmpty()) {
            return null;
        }
        return targetIds.stream().map(String::valueOf).collect(Collectors.joining(","));
    }
}
