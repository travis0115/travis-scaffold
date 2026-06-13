package com.travis.monolith.system.notice.api;

import java.util.Collection;

/** 通知模块对外发布 API。 */
public interface SysNoticeApi {

    void publishToUsers(String title, String content, Collection<Long> userIds);
}
