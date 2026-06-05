package com.travis.monolith.system.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.internal.model.entity.SysUpdateLog;
import com.travis.monolith.system.internal.model.request.log.SysUpdateLogReq;
import com.travis.monolith.system.internal.model.response.log.SysUpdateLogResp;
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
