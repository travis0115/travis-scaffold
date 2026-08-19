package com.travis.infrastructure.framework.quartz.core;

/** 可由管理员配置的 Quartz 任务处理器描述。 */
public record QuartzJobHandlerDescriptor(String name, String description) {}
