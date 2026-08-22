package com.travis.monolith.ops.job.internal.model;

import java.util.List;

/** 执行观察器所需的任务配置快照。 */
public record OpsJobExecutionConfig(String jobName, String handlerName, List<Long> alertUserIds) {}
