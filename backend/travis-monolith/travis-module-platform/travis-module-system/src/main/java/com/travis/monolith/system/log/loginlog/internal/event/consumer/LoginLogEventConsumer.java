package com.travis.monolith.system.log.loginlog.internal.event.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travis.monolith.system.log.loginlog.internal.service.SysLoginLogService;
import com.travis.monolith.system.user.api.event.UserLoginEvent;
import java.nio.ByteBuffer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.annotation.RocketMQMessageListener;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.apache.rocketmq.client.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 登录日志事件消费者，通过 RocketMQ 接收 {@link UserLoginEvent} 并调用登录日志服务进行持久化。
 *
 * <p>日志持久化使用独立事务（REQUIRES_NEW），确保登录失败时外层事务回滚不影响日志记录。
 *
 * @author travis
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "system-event",
        tag = "user-login",
        consumerGroup = "system-user-login-consumer")
@RequiredArgsConstructor
public class LoginLogEventConsumer implements RocketMQListener {

    private final SysLoginLogService loginLogService;
    private final ObjectMapper objectMapper;

    @Override
    public ConsumeResult consume(MessageView messageView) {
        try {
            ByteBuffer buf = messageView.getBody();
            byte[] body = new byte[buf.remaining()];
            buf.get(body);
            UserLoginEvent event = objectMapper.readValue(body, UserLoginEvent.class);
            loginLogService.recordLoginLog(
                    event.getUsername(), event.getStatus(), event.getMessage());
            return ConsumeResult.SUCCESS;
        } catch (Exception e) {
            log.error("消费用户登录事件失败", e);
            return ConsumeResult.FAILURE;
        }
    }
}
