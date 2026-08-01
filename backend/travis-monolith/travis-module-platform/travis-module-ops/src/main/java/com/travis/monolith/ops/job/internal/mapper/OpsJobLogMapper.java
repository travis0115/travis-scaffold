package com.travis.monolith.ops.job.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.ops.job.internal.entity.OpsJobLog;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OpsJobLogMapper extends BaseMapperX<OpsJobLog> {

    /** 根据任务编号物理删除调度日志。 */
    int deletePhysicallyByJobId(@Param("jobId") Long jobId);

    /** 物理删除全部调度日志。 */
    int deleteAllPhysically();

    /** 物理删除指定任务在截止时间之前的调度日志。 */
    int deleteExpiredPhysically(@Param("jobId") Long jobId, @Param("before") LocalDateTime before);

    /** 将 Quartz 中已不存在执行记录的运行中日志标记为中断失败。 */
    int markInterruptedExecutions(@Param("endTime") LocalDateTime endTime);
}
