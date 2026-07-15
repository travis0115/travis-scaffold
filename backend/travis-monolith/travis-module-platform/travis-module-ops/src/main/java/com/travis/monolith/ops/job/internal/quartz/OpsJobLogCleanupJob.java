package com.travis.monolith.ops.job.internal.quartz;

import com.travis.monolith.ops.job.internal.service.OpsJobLogService;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

/** 定期清理过期任务执行日志的 Quartz 内部任务。 */
@RequiredArgsConstructor
public class OpsJobLogCleanupJob implements Job {

    private final OpsJobLogService logService;

    @Override
    public void execute(JobExecutionContext context) {
        logService.cleanExpired();
    }
}
