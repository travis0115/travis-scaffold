package com.travis.monolith.system.log.updatelog.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.log.updatelog.api.request.SysUpdateLogReq;
import com.travis.monolith.system.log.updatelog.api.response.SysUpdateLogResp;
import com.travis.monolith.system.log.updatelog.internal.entity.SysUpdateLog;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/**
 * 更新日志对象转换器
 *
 * @author travis
 */
@Mapper(config = BaseMapperConfig.class)
public interface SysUpdateLogConverter {

    SysUpdateLogResp toResp(SysUpdateLog updateLog);

    List<SysUpdateLogResp> toRespList(List<SysUpdateLog> updateLogs);

    SysUpdateLog toEntity(SysUpdateLogReq req);

    void update(SysUpdateLogReq req, @MappingTarget SysUpdateLog updateLog);
}
