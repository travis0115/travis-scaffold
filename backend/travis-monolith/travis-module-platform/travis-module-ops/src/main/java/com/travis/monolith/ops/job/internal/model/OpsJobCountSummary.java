package com.travis.monolith.ops.job.internal.model;

/** 定时任务数量汇总。 */
public record OpsJobCountSummary(long total, long enabled) {}
