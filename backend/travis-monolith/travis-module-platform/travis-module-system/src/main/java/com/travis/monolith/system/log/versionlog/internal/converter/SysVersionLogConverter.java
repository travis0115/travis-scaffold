package com.travis.monolith.system.log.versionlog.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.log.versionlog.api.request.SysVersionLogCreateReq;
import com.travis.monolith.system.log.versionlog.api.request.SysVersionLogUpdateReq;
import com.travis.monolith.system.log.versionlog.api.response.SysVersionLogDetailResp;
import com.travis.monolith.system.log.versionlog.api.response.SysVersionLogPageResp;
import com.travis.monolith.system.log.versionlog.api.response.SysVersionLogPublishedResp;
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

    SysVersionLogPageResp toResp(SysVersionLog versionLog);

    SysVersionLogDetailResp toDetailResp(SysVersionLog versionLog);

    List<SysVersionLogPublishedResp> toPublishedRespList(List<SysVersionLog> versionLogs);

    List<SysVersionLogPageResp> toRespList(List<SysVersionLog> versionLogs);

    SysVersionLog toEntity(SysVersionLogCreateReq req);

    void update(SysVersionLogUpdateReq req, @MappingTarget SysVersionLog versionLog);
}
