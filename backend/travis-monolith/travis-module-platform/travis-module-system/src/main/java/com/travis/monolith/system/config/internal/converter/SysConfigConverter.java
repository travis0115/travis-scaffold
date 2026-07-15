package com.travis.monolith.system.config.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.config.api.request.SysConfigCreateReq;
import com.travis.monolith.system.config.api.request.SysConfigUpdateReq;
import com.travis.monolith.system.config.api.response.SysConfigResp;
import com.travis.monolith.system.config.internal.entity.SysConfig;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/**
 * 系统配置对象转换器
 *
 * @author travis
 */
@Mapper(config = BaseMapperConfig.class)
public interface SysConfigConverter {

    /** 将系统配置实体转换为响应。 */
    SysConfigResp toResp(SysConfig config);

    /** 批量将系统配置实体转换为响应。 */
    List<SysConfigResp> toRespList(List<SysConfig> configs);

    /** 将创建参数转换为系统配置实体。 */
    SysConfig toEntity(SysConfigCreateReq req);

    /** 将更新参数写入已有系统配置实体。 */
    void update(SysConfigUpdateReq req, @MappingTarget SysConfig config);
}
