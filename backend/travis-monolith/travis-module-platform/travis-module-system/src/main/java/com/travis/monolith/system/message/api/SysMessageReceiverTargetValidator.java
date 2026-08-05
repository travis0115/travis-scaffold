package com.travis.monolith.system.message.api;

import java.util.Collection;
import java.util.Set;

/** 跨登录体系校验消息指定用户接收对象。 */
public interface SysMessageReceiverTargetValidator {

    /** 返回支持的接收端登录体系。 */
    String getReceiverType();

    /** 返回真实存在且可接收消息的用户 ID。 */
    Set<Long> findExistingUserIds(Collection<Long> userIds);
}
