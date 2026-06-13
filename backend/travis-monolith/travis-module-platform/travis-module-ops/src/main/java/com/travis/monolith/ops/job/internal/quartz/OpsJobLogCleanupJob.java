package com.travis.monolith.ops.job.internal.quartz;

import com.travis.monolith.ops.job.internal.service.OpsJobLogService;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

@RequiredArgsConstructor
public class OpsJobLogCleanupJob implements Job {

    private final OpsJobLogService logService;

    @Override
    public void execute(JobExecutionContext context) {
        logService.cleanExpired();
    }
}
