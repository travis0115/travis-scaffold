package com.travis.monolith.ops.job.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.ops.job.internal.entity.OpsJob;
import com.travis.monolith.ops.job.internal.model.OpsJobCountSummary;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OpsJobMapper extends BaseMapperX<OpsJob> {

    /** 查询定时任务数量汇总。 */
    OpsJobCountSummary selectCountSummary();
}
