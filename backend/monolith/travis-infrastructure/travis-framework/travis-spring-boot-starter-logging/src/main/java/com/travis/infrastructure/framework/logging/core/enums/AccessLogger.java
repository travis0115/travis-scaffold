package com.travis.infrastructure.framework.logging.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;

/**
 * Access Logger 枚举
 */
@AllArgsConstructor
@Slf4j
public enum AccessLogger {
    /**
     * 控制台
     */
    STDOUT("LOGGER_STDOUT_ACCESS"),

    /**
     * 文件
     */
    FILE("LOGGER_FILE_ACCESS"),

    ;

    @Getter
    private final String loggerName;

    public static AccessLogger from(String output) {
        String normalizedOutput = output.trim().toLowerCase(Locale.ROOT);
        return switch (normalizedOutput) {
            case "stdout" -> STDOUT;
            case "file" -> FILE;
            default -> {
                log.warn("无法识别的logger output：{}", output);
                yield STDOUT;
            }
        };
    }
}
