package com.travis.monolith.ops.job.internal.model;

import java.time.LocalDate;

/** 定时任务执行日志单日趋势。 */
public record OpsJobLogTrendPoint(LocalDate date, long success, long failed) {}
