package com.travis.monolith.system.file.api.event;

/** 文件上传人用户名变更事件。 */
public record UploaderNameChangedEvent(String uploaderType, Long uploaderId, String uploaderName) {}
