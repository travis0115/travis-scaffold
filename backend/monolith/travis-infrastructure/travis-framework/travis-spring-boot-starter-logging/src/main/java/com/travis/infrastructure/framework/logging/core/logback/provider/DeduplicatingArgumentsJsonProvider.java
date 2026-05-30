package com.travis.infrastructure.framework.logging.core.logback.provider;

import ch.qos.logback.classic.spi.ILoggingEvent;
import net.logstash.logback.argument.StructuredArgument;
import net.logstash.logback.composite.loggingevent.ArgumentsJsonProvider;
import tools.jackson.core.JsonGenerator;

/**
 * 替代 logstash-logback-encoder 默认的 {@code <arguments/>} provider。
 * 被 message 中占位符 {@code {}} 消费掉的参数不再在 arguments 中重复输出，避免同一内容在 message 与 arguments 中重复。
 * 参数值的脱敏由 Jackson 的 DesensitizeJacksonModule 或调用方传入的已脱敏值负责，该类不参与脱敏。
 */
public class DeduplicatingArgumentsJsonProvider extends ArgumentsJsonProvider {

    @Override
    public void writeTo(JsonGenerator generator, ILoggingEvent event) {
        if (!isIncludeStructuredArguments() && !isIncludeNonStructuredArguments()) {
            // Short-circuit if nothing is included
            return;
        }
        var args = event.getArgumentArray();
        if (args == null || args.length == 0) return;

        var consumedByMessage = countPlaceholders(event.getMessage());
        var hasWrittenFieldName = false;

        for (int argIndex = 0; argIndex < args.length; argIndex++) {
            if (argIndex < consumedByMessage) {
                continue;
            }
            var arg = args[argIndex];

            if (arg instanceof StructuredArgument structuredArgument) {
                if (isIncludeStructuredArguments()) {
                    if (!hasWrittenFieldName && getFieldName() != null) {
                        generator.writeObjectPropertyStart(getFieldName());
                        hasWrittenFieldName = true;
                    }
                    structuredArgument.writeTo(generator);
                }
            } else if (isIncludeNonStructuredArguments()) {
                if (!hasWrittenFieldName && getFieldName() != null) {
                    generator.writeObjectPropertyStart(getFieldName());
                    hasWrittenFieldName = true;
                }
                var fieldName = getNonStructuredArgumentsFieldPrefix() + argIndex;
                generator.writePOJOProperty(fieldName, arg);
            }
        }

        if (hasWrittenFieldName) {
            generator.writeEndObject();
        }
    }

    /**
     * 统计 message 模板中未被转义的 {@code {}} 占位符个数，即被 message 消费掉的参数个数。
     */
    private static int countPlaceholders(String message) {
        if (message == null) return 0;
        var count = 0;
        var idx = 0;
        while ((idx = message.indexOf("{}", idx)) != -1) {
            if (idx > 0 && message.charAt(idx - 1) == '\\') {
                idx += 2;
                continue;
            }
            count++;
            idx += 2;
        }
        return count;
    }
}