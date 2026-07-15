package com.travis.monolith.system.file.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.file.api.response.SysFileResp;
import com.travis.monolith.system.file.internal.entity.SysFile;
import org.mapstruct.Mapper;

/** 文件对象转换器。 */
@Mapper(config = BaseMapperConfig.class)
public interface SysFileConverter {

    /** 将文件实体转换为响应。 */
    SysFileResp toResp(SysFile file);
}
