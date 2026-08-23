package com.travis.monolith.ops.errorlog.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.ops.errorlog.api.response.SysErrorLogOccurrenceResp;
import com.travis.monolith.ops.errorlog.api.response.SysErrorLogResp;
import com.travis.monolith.ops.errorlog.internal.entity.SysErrorLog;
import com.travis.monolith.ops.errorlog.internal.entity.SysErrorLogOccurrence;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** 系统异常日志对象转换器。 */
@Mapper(config = BaseMapperConfig.class)
public interface SysErrorLogConverter {

    /** 将异常日志实体转换为响应。 */
    @Mapping(target = "occurrences", ignore = true)
    SysErrorLogResp toResp(SysErrorLog errorLog);

    /** 将发生明细实体转换为响应。 */
    SysErrorLogOccurrenceResp toOccurrenceResp(SysErrorLogOccurrence occurrence);
}
