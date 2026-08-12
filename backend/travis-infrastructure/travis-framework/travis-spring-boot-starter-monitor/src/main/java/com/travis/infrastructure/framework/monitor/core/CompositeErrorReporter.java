package com.travis.infrastructure.framework.monitor.core;

import com.travis.infrastructure.common.monitor.error.ErrorEvent;
import com.travis.infrastructure.common.monitor.error.ErrorReporter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/** 将异常事件依次分发给全部上报渠道。 */
@Slf4j
public class CompositeErrorReporter implements ErrorReporter {

    private final List<ErrorReporterContributor> contributors;

    public CompositeErrorReporter(List<ErrorReporterContributor> contributors) {
        this.contributors = List.copyOf(contributors);
    }

    @Override
    public void report(ErrorEvent event) {
        for (var contributor : contributors) {
            try {
                contributor.report(event);
            } catch (Exception exception) {
                log.error(
                        "异常上报渠道执行失败，contributor={}, sourceType={}, sourceName={}",
                        contributor.getClass().getName(),
                        event.sourceType(),
                        event.sourceName(),
                        exception);
            }
        }
    }
}
