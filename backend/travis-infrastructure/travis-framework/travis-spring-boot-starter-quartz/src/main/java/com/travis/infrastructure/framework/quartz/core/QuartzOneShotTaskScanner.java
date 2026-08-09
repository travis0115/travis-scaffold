package com.travis.infrastructure.framework.quartz.core;

import java.util.function.Consumer;

/** 分批扫描业务期望的一次性 Quartz 任务。 */
@FunctionalInterface
public interface QuartzOneShotTaskScanner {

    void scan(Consumer<QuartzOneShotTask> taskConsumer);
}
