package com.travis.monolith.system.config.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.config.api.response.SysConfigResp;
import com.travis.monolith.system.config.internal.entity.SysConfig;
import com.travis.monolith.system.config.internal.request.SysConfigReq;
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

    SysConfigResp toResp(SysConfig config);

    List<SysConfigResp> toRespList(List<SysConfig> configs);

    SysConfig toEntity(SysConfigReq req);

    void update(SysConfigReq req, @MappingTarget SysConfig config);
}
