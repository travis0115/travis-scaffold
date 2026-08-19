package com.travis.monolith.ops.job.api.response;

/** 可由管理员配置的任务处理器。 */
public record OpsJobHandlerResp(String name, String description) {}
