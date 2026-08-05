package com.travis.monolith.system.user.api.event;

/** 用户部门变化后触发消息未读数缓存失效；userId 为空表示全部后台用户。 */
public record UserMessageAudienceChangedEvent(Long userId) {}
