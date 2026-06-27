package com.travis.monolith.system.file.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.file.api.response.SysFileResp;
import com.travis.monolith.system.file.internal.entity.SysFile;
import org.mapstruct.Mapper;

@Mapper(config = BaseMapperConfig.class)
public interface SysFileConverter {

    SysFileResp toResp(SysFile file);
}
