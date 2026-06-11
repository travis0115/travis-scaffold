package com.travis.monolith.system.log.versionlog.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.log.versionlog.api.request.SysVersionLogReq;
import com.travis.monolith.system.log.versionlog.api.response.SysVersionLogResp;
import com.travis.monolith.system.log.versionlog.internal.entity.SysVersionLog;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/**
 * 版本日志对象转换器
 *
 * @author travis
 */
@Mapper(config = BaseMapperConfig.class)
public interface SysVersionLogConverter {

    SysVersionLogResp toResp(SysVersionLog versionLog);

    List<SysVersionLogResp> toRespList(List<SysVersionLog> versionLogs);

    SysVersionLog toEntity(SysVersionLogReq req);

    void update(SysVersionLogReq req, @MappingTarget SysVersionLog versionLog);
}
