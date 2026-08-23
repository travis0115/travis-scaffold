package com.travis.monolith.ops.job.internal.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.ops.job.internal.entity.OpsJobLog;
import com.travis.monolith.ops.job.internal.model.OpsJobLogStatsSummary;
import com.travis.monolith.ops.job.internal.model.OpsJobLogTrendPoint;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OpsJobLogMapper extends BaseMapperX<OpsJobLog> {

    /** 批量查询每个任务最近一次执行日志。 */
    List<OpsJobLog> selectLatestByJobIds(@Param("jobIds") Collection<Long> jobIds);

    /** 查询指定任务或全部任务的执行日志统计汇总。 */
    OpsJobLogStatsSummary selectStatsSummary(@Param("jobId") Long jobId);

    /** 查询看板时间范围内全部任务的执行汇总。 */
    OpsJobLogStatsSummary selectDashboardStatsSummary(
            @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /** 查询指定任务执行耗时的第 95 百分位。 */
    long selectP95Duration(@Param("jobId") Long jobId);

    /** 查询指定任务当前连续失败次数，忽略仍在运行的执行。 */
    long selectConsecutiveFailures(@Param("jobId") Long jobId);

    /** 查询指定任务从给定日期开始的每日执行趋势。 */
    List<OpsJobLogTrendPoint> selectTrend(
            @Param("jobId") Long jobId, @Param("startTime") LocalDateTime startTime);

    /** 查询看板时间范围内全部任务的每日执行趋势。 */
    List<OpsJobLogTrendPoint> selectDashboardTrend(
            @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

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
