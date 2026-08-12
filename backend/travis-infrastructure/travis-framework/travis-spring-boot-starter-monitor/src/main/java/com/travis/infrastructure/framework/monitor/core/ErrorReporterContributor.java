package com.travis.infrastructure.framework.monitor.core;

import com.travis.infrastructure.common.monitor.error.ErrorEvent;

/** 单个异常上报渠道，由 {@link CompositeErrorReporter} 统一编排。 */
@FunctionalInterface
public interface ErrorReporterContributor {

    /** 向当前渠道上报异常事件。 */
    void report(ErrorEvent event);
}
