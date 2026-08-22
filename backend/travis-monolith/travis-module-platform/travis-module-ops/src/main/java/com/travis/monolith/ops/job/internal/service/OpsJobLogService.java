package com.travis.monolith.ops.job.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.ops.job.api.request.OpsJobLogPageReq;
import com.travis.monolith.ops.job.api.response.OpsJobLogDetailResp;
import com.travis.monolith.ops.job.api.response.OpsJobLogPageResp;
import com.travis.monolith.ops.job.api.response.OpsJobStatsResp;
import com.travis.monolith.ops.job.internal.entity.OpsJobLog;
import java.util.Collection;
import java.util.Map;

/** 定时任务执行日志服务。 */
public interface OpsJobLogService extends IService<OpsJobLog> {
    /** 分页查询任务执行日志。 */
    PageResp<OpsJobLogPageResp> page(OpsJobLogPageReq req);

    /** 查询执行日志详情，日志不存在时抛出业务异常。 */
    OpsJobLogDetailResp getOrThrow(Long id);

    /** 清理指定任务或全部任务的执行日志。 */
    void clean(Long jobId);

    /** 按各任务的保留期限清理过期执行日志。 */
    void cleanExpired();

    /** 收敛因执行节点中断而遗留的运行中日志。 */
    void markInterruptedExecutions();

    /** 统计指定任务的执行情况。 */
    OpsJobStatsResp stats(Long jobId);

    /** 新增任务执行日志。 */
    void saveExecution(OpsJobLog log);

    /** 更新任务执行结果。 */
    void updateExecution(OpsJobLog log);

    /** 使指定任务的执行统计缓存失效。 */
    void invalidateStats(Long jobId);

    /** 批量查询各任务最近一次执行日志。 */
    Map<Long, OpsJobLog> latestByJobIds(Collection<Long> jobIds);
}
