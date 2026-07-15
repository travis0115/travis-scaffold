package com.travis.monolith.system.file.api.event;

/**
 * 文件上传人用户名变更事件。
 *
 * @param uploaderType 上传主体类型
 * @param uploaderId 上传主体 ID
 * @param uploaderName 变更后的展示名称
 */
public record UploaderNameChangedEvent(String uploaderType, Long uploaderId, String uploaderName) {}
