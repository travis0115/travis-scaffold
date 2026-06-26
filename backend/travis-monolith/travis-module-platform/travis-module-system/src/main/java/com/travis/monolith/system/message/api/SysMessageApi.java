package com.travis.monolith.system.message.api;

import java.util.Collection;

/** 消息推送模块对外发布 API。 */
public interface SysMessageApi {

    void publishToUsers(String title, String content, Collection<Long> userIds);

    void publishToUsers(
            String title,
            String content,
            Collection<Long> userIds,
            String sourceType,
            String sourceId);

    void publishToUsers(
            String receiverType,
            String title,
            String content,
            Collection<Long> userIds,
            String sourceType,
            String sourceId);
}
