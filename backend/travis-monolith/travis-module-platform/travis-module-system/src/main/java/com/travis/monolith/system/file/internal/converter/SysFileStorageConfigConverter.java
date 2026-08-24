package com.travis.monolith.system.file.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.file.api.request.SysFileStorageConfigCreateReq;
import com.travis.monolith.system.file.api.request.SysFileStorageConfigUpdateReq;
import com.travis.monolith.system.file.api.response.SysFileStorageConfigResp;
import com.travis.monolith.system.file.internal.entity.SysFileStorageConfig;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/** 文件存储配置对象转换器。 */
@Mapper(config = BaseMapperConfig.class)
public interface SysFileStorageConfigConverter {

    /** 将存储配置实体转换为响应。 */
    @Mapping(
            target = "secretConfigured",
            expression = "java(cn.hutool.core.util.StrUtil.isNotBlank(entity.getSecretKey()))")
    SysFileStorageConfigResp toResp(SysFileStorageConfig entity);

    /** 批量将存储配置实体转换为响应。 */
    List<SysFileStorageConfigResp> toRespList(List<SysFileStorageConfig> entities);

    /** 将创建参数转换为存储配置实体。 */
    SysFileStorageConfig toEntity(SysFileStorageConfigCreateReq req);

    /** 将更新参数转换为存储配置实体。 */
    SysFileStorageConfig toEntity(SysFileStorageConfigUpdateReq req);

    /** 将更新参数写入已有存储配置实体。 */
    SysFileStorageConfig update(
            SysFileStorageConfigUpdateReq req, @MappingTarget SysFileStorageConfig config);
}
