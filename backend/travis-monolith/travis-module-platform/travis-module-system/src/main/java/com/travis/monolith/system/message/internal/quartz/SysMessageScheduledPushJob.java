package com.travis.monolith.system.message.internal.quartz;

import com.travis.monolith.system.message.internal.service.SysMessageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/** 到期定时消息推送任务。 */
@Slf4j
@AllArgsConstructor
@DisallowConcurrentExecution
public class SysMessageScheduledPushJob implements Job {
    /** Quartz JobDataMap 中的消息 ID。 */
    public static final String DATA_MESSAGE_ID = "messageId";

    private final SysMessageService messageService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        long messageId = context.getMergedJobDataMap().getLong(DATA_MESSAGE_ID);
        try {
            if (messageService.pushScheduled(messageId)) {
                log.info("[消息推送] 已推送定时消息，messageId={}", messageId);
            }
        } catch (Exception exception) {
            throw new JobExecutionException(exception, false);
        }
    }
}
