package com.travis.monolith.system.internal.converter;

import com.travis.monolith.system.internal.model.entity.SysConfig;
import com.travis.monolith.system.internal.model.req.SysConfigReq;
import com.travis.monolith.system.internal.model.resp.SysConfigResp;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 系统配置对象转换器
 *
 * @author travis
 */
@Mapper(componentModel = "spring")
public interface SysConfigConverter {

    SysConfigResp toConfigResp(SysConfig config);

    List<SysConfigResp> toConfigRespList(List<SysConfig> configs);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    SysConfig toConfigEntity(SysConfigReq req);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateConfigFromReq(SysConfigReq req, @MappingTarget SysConfig config);
}
