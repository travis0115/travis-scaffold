package com.travis.monolith.system.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.internal.model.entity.SysConfig;
import com.travis.monolith.system.internal.model.request.config.SysConfigReq;
import com.travis.monolith.system.internal.model.response.config.SysConfigResp;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 系统配置对象转换器
 *
 * @author travis
 */
@Mapper(config = BaseMapperConfig.class)
public interface SysConfigConverter {

    SysConfigResp toResp(SysConfig config);

    List<SysConfigResp> toRespList(List<SysConfig> configs);

    SysConfig toEntity(SysConfigReq req);

    void update(SysConfigReq req, @MappingTarget SysConfig config);
}
