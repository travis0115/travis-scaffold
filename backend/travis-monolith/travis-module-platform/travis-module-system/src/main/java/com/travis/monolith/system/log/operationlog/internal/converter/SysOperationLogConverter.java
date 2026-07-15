package com.travis.monolith.system.log.operationlog.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.log.operationlog.api.event.OperationLogEvent;
import com.travis.monolith.system.log.operationlog.api.response.SysOperationLogResp;
import com.travis.monolith.system.log.operationlog.internal.entity.SysOperationLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 操作日志转换器
 *
 * @author travis
 */
@Mapper(config = BaseMapperConfig.class)
public interface SysOperationLogConverter {

    /** 将操作日志事件及用户名转换为日志实体。 */
    @Mapping(target = "username", source = "username")
    SysOperationLog toEntity(OperationLogEvent event, String username);

    /** 将操作日志实体转换为响应。 */
    SysOperationLogResp toResp(SysOperationLog operationLog);
}
