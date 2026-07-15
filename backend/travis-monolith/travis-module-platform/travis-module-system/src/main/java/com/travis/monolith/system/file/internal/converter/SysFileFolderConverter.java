package com.travis.monolith.system.file.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.file.api.request.SysFileFolderCreateReq;
import com.travis.monolith.system.file.api.request.SysFileFolderUpdateReq;
import com.travis.monolith.system.file.internal.entity.SysFileFolder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/** 文件夹对象转换器。 */
@Mapper(config = BaseMapperConfig.class)
public interface SysFileFolderConverter {

    /** 将创建参数转换为文件夹实体。 */
    SysFileFolder toEntity(SysFileFolderCreateReq req);

    /** 将更新参数写入已有文件夹实体。 */
    void update(SysFileFolderUpdateReq req, @MappingTarget SysFileFolder fileFolder);
}
