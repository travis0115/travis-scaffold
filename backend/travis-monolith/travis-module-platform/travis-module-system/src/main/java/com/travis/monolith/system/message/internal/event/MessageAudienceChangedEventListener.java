package com.travis.monolith.system.message.internal.event;

import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.monolith.system.message.internal.service.impl.SysMessageInboxCache;
import com.travis.monolith.system.role.api.event.RoleMessageAudienceChangedEvent;
import com.travis.monolith.system.user.api.event.UserMessageAudienceChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** 用户部门或角色变化后使相关后台用户未读数缓存失效。 */
@Component
@RequiredArgsConstructor
public class MessageAudienceChangedEventListener {
    private final SysMessageInboxCache inboxCache;

    @EventListener
    public void onUserAudienceChanged(UserMessageAudienceChangedEvent event) {
        invalidate(event.userId());
    }

    @EventListener
    public void onRoleAudienceChanged(RoleMessageAudienceChangedEvent event) {
        invalidate(event.userId());
    }

    private void invalidate(Long userId) {
        if (userId == null) {
            inboxCache.invalidateReceiver(LoginType.ADMIN);
            return;
        }
        inboxCache.invalidateUser(LoginType.ADMIN, userId);
    }
}
