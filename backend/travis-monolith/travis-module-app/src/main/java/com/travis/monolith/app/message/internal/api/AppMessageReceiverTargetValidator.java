package com.travis.monolith.app.message.internal.api;

import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.monolith.app.user.internal.service.AppUserService;
import com.travis.monolith.system.message.api.SysMessageReceiverTargetValidator;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** App 用户消息接收对象校验器。 */
@Component
@RequiredArgsConstructor
public class AppMessageReceiverTargetValidator implements SysMessageReceiverTargetValidator {
    private final AppUserService userService;

    @Override
    public String getReceiverType() {
        return LoginType.APP;
    }

    @Override
    public Set<Long> findExistingUserIds(Collection<Long> userIds) {
        return userService.listOptionsByIds(userIds).stream()
                .map(option -> option.getId())
                .collect(Collectors.toSet());
    }
}
