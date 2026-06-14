package com.travis.monolith.system.file.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.file.api.request.SysFileStorageConfigCreateReq;
import com.travis.monolith.system.file.api.request.SysFileStorageConfigUpdateReq;
import com.travis.monolith.system.file.internal.entity.SysFileStorageConfig;
import org.mapstruct.Mapper;

@Mapper(config = BaseMapperConfig.class)
public interface SysFileStorageConfigConverter {

    SysFileStorageConfig toEntity(SysFileStorageConfigCreateReq req);

    SysFileStorageConfig toEntity(SysFileStorageConfigUpdateReq req);
}
