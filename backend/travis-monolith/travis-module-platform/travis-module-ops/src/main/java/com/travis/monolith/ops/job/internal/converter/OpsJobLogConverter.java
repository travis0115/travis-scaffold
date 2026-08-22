package com.travis.monolith.ops.job.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.ops.job.api.response.OpsJobLogDetailResp;
import com.travis.monolith.ops.job.api.response.OpsJobLogPageResp;
import com.travis.monolith.ops.job.internal.entity.OpsJobLog;
import org.mapstruct.Mapper;

/** 定时任务执行日志对象转换器。 */
@Mapper(config = BaseMapperConfig.class)
public interface OpsJobLogConverter {

    /** 将执行日志实体转换为分页响应。 */
    OpsJobLogPageResp toPageResp(OpsJobLog log);

    /** 将执行日志实体转换为详情响应。 */
    OpsJobLogDetailResp toDetailResp(OpsJobLog log);
}
