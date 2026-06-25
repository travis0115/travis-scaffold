package com.travis.monolith.system.version.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.version.api.request.SysVersionCreateReq;
import com.travis.monolith.system.version.api.request.SysVersionUpdateReq;
import com.travis.monolith.system.version.api.response.SysVersionResp;
import com.travis.monolith.system.version.internal.entity.SysVersion;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/**
 * 版本日志对象转换器
 *
 * @author travis
 */
@Mapper(config = BaseMapperConfig.class)
public interface SysVersionConverter {

    SysVersionResp toResp(SysVersion versionLog);

    List<SysVersionResp> toRespList(List<SysVersion> versionLogs);

    SysVersion toEntity(SysVersionCreateReq req);

    void update(SysVersionUpdateReq req, @MappingTarget SysVersion versionLog);
}
