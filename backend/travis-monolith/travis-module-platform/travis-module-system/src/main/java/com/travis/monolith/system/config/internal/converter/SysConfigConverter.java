package com.travis.monolith.system.config.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.config.api.request.SysConfigCreateReq;
import com.travis.monolith.system.config.api.request.SysConfigUpdateReq;
import com.travis.monolith.system.config.api.response.SysConfigDetailResp;
import com.travis.monolith.system.config.api.response.SysConfigPageResp;
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

    SysConfigPageResp toResp(SysConfig config);

    SysConfigDetailResp toDetailResp(SysConfig config);

    List<SysConfigPageResp> toRespList(List<SysConfig> configs);

    SysConfig toEntity(SysConfigCreateReq req);

    void update(SysConfigUpdateReq req, @MappingTarget SysConfig config);
}
