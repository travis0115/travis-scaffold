package com.travis.monolith.system.user.internal.event;

import com.travis.infrastructure.framework.rocketmq.core.AbstractEventListener;
import com.travis.monolith.system.common.api.SystemEventConstant;
import com.travis.monolith.system.user.api.event.UserOnlinePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

/**
 * 用户在线状态变更事件消费者。
 *
 * <p>消费 {@code system-normal-event:user-online-status} 消息，处理用户上线/下线后的业务逻辑。
 *
 * <p>当前实现仅记录日志，后续可扩展：
 *
 * <ul>
 *   <li>更新数据库中的用户在线状态字段
 *   <li>推送"用户上线/下线"通知给相关管理员
 *   <li>更新在线用户列表缓存
 * </ul>
 *
 * @author travis
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = SystemEventConstant.NORMAL_EVENT,
        tag = SystemEventConstant.USER_ONLINE_STATUS,
        consumerGroup = EventConsumerGroup.USER_ONLINE_STATUS_CONSUMER_GROUP)
@RequiredArgsConstructor
public class UserOnlineStatusEventListener extends AbstractEventListener<UserOnlinePayload> {

    @Override
    protected void onEvent(UserOnlinePayload payload) {
        if (payload.online()) {
            log.info(
                    "[OnlineStatus] 用户上线: loginType={}, userId={}",
                    payload.loginType(),
                    payload.userId());
            // TODO: 更新数据库在线状态、推送通知等
        } else {
            log.info(
                    "[OnlineStatus] 用户下线: loginType={}, userId={}",
                    payload.loginType(),
                    payload.userId());
            // TODO: 更新数据库在线状态、推送通知等
        }
    }
}
