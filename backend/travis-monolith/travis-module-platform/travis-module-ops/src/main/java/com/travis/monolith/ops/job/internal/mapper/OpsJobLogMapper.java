package com.travis.monolith.ops.job.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.ops.job.internal.entity.OpsJobLog;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OpsJobLogMapper extends BaseMapperX<OpsJobLog> {

    @Delete("DELETE FROM ops_job_log WHERE job_id = #{jobId}")
    int deletePhysicallyByJobId(@Param("jobId") Long jobId);

    @Delete("DELETE FROM ops_job_log")
    int deleteAllPhysically();

    @Delete("DELETE FROM ops_job_log WHERE job_id = #{jobId} AND create_time < #{before}")
    int deleteExpiredPhysically(@Param("jobId") Long jobId, @Param("before") LocalDateTime before);
}
