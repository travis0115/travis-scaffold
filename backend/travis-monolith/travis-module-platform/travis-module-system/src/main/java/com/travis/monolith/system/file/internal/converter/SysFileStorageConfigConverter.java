package com.travis.monolith.system.file.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.file.api.request.SysFileStorageConfigCreateReq;
import com.travis.monolith.system.file.api.request.SysFileStorageConfigUpdateReq;
import com.travis.monolith.system.file.api.response.SysFileStorageConfigResp;
import com.travis.monolith.system.file.internal.entity.SysFileStorageConfig;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = BaseMapperConfig.class)
public interface SysFileStorageConfigConverter {

    SysFileStorageConfigResp toResp(SysFileStorageConfig entity);

    List<SysFileStorageConfigResp> toRespList(List<SysFileStorageConfig> entities);

    SysFileStorageConfig toEntity(SysFileStorageConfigCreateReq req);

    SysFileStorageConfig toEntity(SysFileStorageConfigUpdateReq req);

    SysFileStorageConfig update(
            SysFileStorageConfigUpdateReq req, @MappingTarget SysFileStorageConfig config);
}
