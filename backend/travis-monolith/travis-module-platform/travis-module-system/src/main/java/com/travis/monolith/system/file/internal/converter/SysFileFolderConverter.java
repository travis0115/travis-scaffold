package com.travis.monolith.system.file.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.file.api.request.SysFileFolderCreateReq;
import com.travis.monolith.system.file.api.request.SysFileFolderUpdateReq;
import com.travis.monolith.system.file.internal.entity.SysFileFolder;
import org.mapstruct.Mapper;

@Mapper(config = BaseMapperConfig.class)
public interface SysFileFolderConverter {

    SysFileFolder toEntity(SysFileFolderCreateReq req);

    SysFileFolder toEntity(SysFileFolderUpdateReq req);
}
