package com.travis.monolith.system.message.internal.event;

import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.monolith.system.message.internal.service.SysMessageReceiverService;
import com.travis.monolith.system.role.api.event.RoleMessageAudienceChangedEvent;
import com.travis.monolith.system.user.api.event.UserMessageAudienceChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/** 用户部门或角色变化后使相关后台用户未读数缓存失效。 */
@Component
@RequiredArgsConstructor
public class MessageAudienceChangedEventListener {
    private final SysMessageReceiverService messageReceiverService;

    @ApplicationModuleListener
    public void onUserAudienceChanged(UserMessageAudienceChangedEvent event) {
        invalidate(event.userId());
    }

    @ApplicationModuleListener
    public void onRoleAudienceChanged(RoleMessageAudienceChangedEvent event) {
        invalidate(event.userId());
    }

    private void invalidate(Long userId) {
        if (userId == null) {
            messageReceiverService.evictUnreadCache(LoginType.ADMIN, null, null);
            return;
        }
        messageReceiverService.evictUserUnreadCache(LoginType.ADMIN, userId);
    }
}
