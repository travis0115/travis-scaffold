package com.travis.monolith.ops.job.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.ops.job.api.response.OpsJobLogResp;
import com.travis.monolith.ops.job.internal.entity.OpsJobLog;
import org.mapstruct.Mapper;

/** 定时任务执行日志对象转换器。 */
@Mapper(config = BaseMapperConfig.class)
public interface OpsJobLogConverter {

    /** 将执行日志实体转换为响应。 */
    OpsJobLogResp toResp(OpsJobLog log);
}
