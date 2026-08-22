package com.travis.monolith.ops.job.internal.service;

import com.travis.infrastructure.framework.jackson.core.JsonUtil;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.redis.core.util.RedisUtil;
import com.travis.monolith.ops.job.api.enums.OpsJobLogStatus;
import com.travis.monolith.ops.job.api.enums.OpsJobStatus;
import com.travis.monolith.ops.job.api.response.OpsJobDashboardResp;
import com.travis.monolith.ops.job.internal.entity.OpsJob;
import com.travis.monolith.ops.job.internal.entity.OpsJobLog;
import com.travis.monolith.ops.job.internal.mapper.OpsJobLogMapper;
import com.travis.monolith.ops.job.internal.mapper.OpsJobMapper;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** 汇总任务及执行日志数据，生成调度看板。 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OpsJobDashboardService {

    private static final String CACHE_KEY = "ops:job:dashboard";
    private static final long CACHE_MILLIS = TimeUnit.MINUTES.toMillis(10);

    private final OpsJobMapper jobMapper;
    private final OpsJobLogMapper jobLogMapper;

    /** 汇总任务调度看板数据。 */
    public OpsJobDashboardResp dashboard() {
        Object cached = getCache();
        if (cached instanceof String value) {
            return JsonUtil.parseObject(value, OpsJobDashboardResp.class);
        }
        long totalJobs = jobMapper.selectCount(new LambdaQueryWrapperX<OpsJob>());
        long enabledJobs =
                jobMapper.selectCount(
                        new LambdaQueryWrapperX<OpsJob>()
                                .eq(OpsJob::getStatus, OpsJobStatus.ENABLED.getValue()));
        long executions = jobLogMapper.selectCount(new LambdaQueryWrapperX<OpsJobLog>());
        long success =
                jobLogMapper.selectCount(
                        new LambdaQueryWrapperX<OpsJobLog>()
                                .eq(OpsJobLog::getStatus, OpsJobLogStatus.SUCCESS.getValue()));
        long failed =
                jobLogMapper.selectCount(
                        new LambdaQueryWrapperX<OpsJobLog>()
                                .eq(OpsJobLog::getStatus, OpsJobLogStatus.FAILED.getValue()));
        var response =
                new OpsJobDashboardResp(
                        totalJobs,
                        enabledJobs,
                        totalJobs - enabledJobs,
                        executions,
                        success,
                        failed,
                        executions == 0 ? 0 : success * 100.0 / executions);
        setCache(response);
        return response;
    }

    /** 使任务调度看板缓存失效。 */
    public void invalidate() {
        try {
            RedisUtil.delete(CACHE_KEY);
        } catch (RuntimeException exception) {
            log.warn("任务调度看板缓存失效失败", exception);
        }
    }

    private Object getCache() {
        try {
            return RedisUtil.get(CACHE_KEY);
        } catch (RuntimeException exception) {
            log.warn("读取任务调度看板缓存失败", exception);
            return null;
        }
    }

    private void setCache(OpsJobDashboardResp value) {
        try {
            RedisUtil.set(CACHE_KEY, JsonUtil.toJsonString(value), CACHE_MILLIS);
        } catch (RuntimeException exception) {
            log.warn("写入任务调度看板缓存失败", exception);
        }
    }
}
