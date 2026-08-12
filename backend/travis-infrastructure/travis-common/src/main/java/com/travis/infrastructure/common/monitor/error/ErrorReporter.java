package com.travis.infrastructure.common.monitor.error;

import com.travis.infrastructure.common.web.constant.MdcKey;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.slf4j.MDC;

/** 系统未预期异常上报端口。 */
@FunctionalInterface
public interface ErrorReporter {

    int MAX_SOURCE_NAME_LENGTH = 200;
    int MAX_BUSINESS_KEY_LENGTH = 100;
    int MAX_MESSAGE_LENGTH = 300;
    int MAX_CONTEXT_LENGTH = 300;
    int MAX_STACK_TRACE_LENGTH = 1200;

    /** 上报已经采集好的异常快照。 */
    void report(ErrorEvent event);

    /** 上报非 HTTP 执行边界中的异常。 */
    default void report(
            ErrorSource sourceType,
            String sourceName,
            String businessKey,
            Throwable throwable) {
        report(
                ErrorEvent.builder()
                          .sourceType(sourceType)
                          .sourceName(truncate(sourceName, MAX_SOURCE_NAME_LENGTH))
                          .businessKey(truncate(businessKey, MAX_BUSINESS_KEY_LENGTH))
                          .requestId(MDC.get(MdcKey.REQUEST_ID))
                          .traceId(MDC.get(MdcKey.TRACE_ID))
                          .exceptionClass(throwable.getClass().getName())
                          .message(truncate(throwable.getMessage(), MAX_MESSAGE_LENGTH))
                          .stackTrace(stackTrace(throwable))
                          .build());
    }

    /** 将文本安全限制在错误事件允许的长度内。 */
    static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    /** 获取限长后的异常堆栈。 */
    static String stackTrace(Throwable throwable) {
        var writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return truncate(writer.toString(), MAX_STACK_TRACE_LENGTH);
    }
}
