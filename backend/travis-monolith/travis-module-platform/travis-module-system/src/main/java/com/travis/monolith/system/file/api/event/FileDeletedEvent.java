package com.travis.monolith.system.file.api.event;

/** 文件元数据删除后清理存储对象的事件。 */
public record FileDeletedEvent(String storageType, String storagePath, String path) {}
