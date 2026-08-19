package com.travis.monolith.ops.job.internal.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.ops.job.internal.entity.OpsJobLog;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OpsJobLogMapper extends BaseMapperX<OpsJobLog> {

    /** 批量查询每个任务最近一次执行日志。 */
    List<OpsJobLog> selectLatestByJobIds(@Param("jobIds") Collection<Long> jobIds);

    /** 根据任务编号物理删除调度日志。 */
    int deletePhysicallyByJobId(@Param("jobId") Long jobId);

    /** 物理删除全部调度日志。 */
    @InterceptorIgnore(blockAttack = "true")
    int deleteAllPhysically();

    /** 物理删除截止时间之前的全部调度日志。 */
    int deleteExpiredPhysicallyAll(@Param("before") LocalDateTime before);

    /** 将 Quartz 中已不存在执行记录的运行中日志标记为中断失败。 */
    int markInterruptedExecutions(@Param("endTime") LocalDateTime endTime);
}
