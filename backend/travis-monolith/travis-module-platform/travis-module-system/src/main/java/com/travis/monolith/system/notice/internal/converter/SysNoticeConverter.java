package com.travis.monolith.system.notice.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.notice.api.request.SysNoticeCreateReq;
import com.travis.monolith.system.notice.api.request.SysNoticeUpdateReq;
import com.travis.monolith.system.notice.internal.entity.SysNotice;
import java.util.Collection;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(config = BaseMapperConfig.class)
public interface SysNoticeConverter {

    @Mapping(target = "targetIds", qualifiedByName = "serializeTargetIds")
    SysNotice toEntity(SysNoticeCreateReq req);

    @Mapping(target = "targetIds", qualifiedByName = "serializeTargetIds")
    void update(SysNoticeUpdateReq req, @MappingTarget SysNotice notice);

    @Named("serializeTargetIds")
    default String serializeTargetIds(Collection<Long> targetIds) {
        if (targetIds == null || targetIds.isEmpty()) {
            return null;
        }
        return targetIds.stream().map(String::valueOf).collect(Collectors.joining(","));
    }
}
