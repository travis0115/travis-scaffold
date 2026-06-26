package com.travis.monolith.system.message.internal.quartz;

import com.travis.monolith.system.message.internal.service.SysMessageService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@Slf4j
public class SysMessageScheduledPushJob implements Job {
    private final SysMessageService messageService;

    public SysMessageScheduledPushJob(SysMessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            int count = messageService.pushDueScheduledMessages();
            if (count > 0) {
                log.info("[消息推送] 已推送到期定时消息 {} 条", count);
            }
        } catch (Exception exception) {
            throw new JobExecutionException(exception, false);
        }
    }
}
