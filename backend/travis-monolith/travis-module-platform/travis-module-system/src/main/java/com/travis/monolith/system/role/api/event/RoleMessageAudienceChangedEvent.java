package com.travis.monolith.system.role.api.event;

/** 用户角色变化后触发消息未读数缓存失效；userId 为空表示全部后台用户。 */
public record RoleMessageAudienceChangedEvent(Long userId) {}
