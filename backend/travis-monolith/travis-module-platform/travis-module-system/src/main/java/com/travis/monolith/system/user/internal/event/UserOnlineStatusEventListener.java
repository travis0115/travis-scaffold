package com.travis.monolith.system.user.internal.event;

import com.travis.monolith.system.user.api.event.UserOnlinePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * 用户在线状态变更事件消费者。
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
@RequiredArgsConstructor
public class UserOnlineStatusEventListener {

    @ApplicationModuleListener
    void onUserOnlineStatusChanged(UserOnlinePayload payload) {
        if (payload.isOnline()) {
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
