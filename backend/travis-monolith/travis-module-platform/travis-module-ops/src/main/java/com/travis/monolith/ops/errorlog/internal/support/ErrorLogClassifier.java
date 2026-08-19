package com.travis.monolith.ops.errorlog.internal.support;

import com.travis.infrastructure.common.monitor.error.ErrorSource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

/** 错误日志聚合分类工具。 */
public final class ErrorLogClassifier {

    private static final String MONOLITH_PACKAGE_PREFIX = "com.travis.monolith.";

    private ErrorLogClassifier() {}

    /** 根据稳定异常定位信息生成 SHA-256 指纹。 */
    public static String fingerprint(
            ErrorSource sourceType, String sourceName, String exceptionClass, String stackTrace) {
        var signature =
                value(sourceType == null ? null : sourceType.name())
                        + '|'
                        + value(sourceName)
                        + '|'
                        + value(exceptionClass)
                        + '|'
                        + firstStackFrame(stackTrace);
        try {
            return HexFormat.of()
                    .formatHex(
                            MessageDigest.getInstance("SHA-256")
                                    .digest(signature.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前 JDK 不支持 SHA-256", exception);
        }
    }

    /** 从来源类名推导 Modulith 业务模块名称。 */
    public static String moduleName(ErrorSource sourceType, String sourceName) {
        if (sourceName != null) {
            int prefixIndex = sourceName.indexOf(MONOLITH_PACKAGE_PREFIX);
            if (prefixIndex >= 0) {
                String[] segments =
                        sourceName
                                .substring(prefixIndex + MONOLITH_PACKAGE_PREFIX.length())
                                .split("\\.");
                if (segments.length >= 2) {
                    return segments[0] + "." + segments[1];
                }
            }
        }
        return sourceType == null ? "unknown" : sourceType.name().toLowerCase(Locale.ROOT);
    }

    /** 根据请求边界识别平台。 */
    public static String platformType(ErrorSource sourceType, String requestUrl) {
        if (sourceType != ErrorSource.WEB || requestUrl == null) {
            return "SYSTEM";
        }
        if (requestUrl.startsWith("/api/admin/")) {
            return "ADMIN";
        }
        if (requestUrl.startsWith("/api/app/")) {
            return "APP";
        }
        return "SYSTEM";
    }

    private static String firstStackFrame(String stackTrace) {
        if (stackTrace == null) {
            return "";
        }
        return stackTrace
                .lines()
                .map(String::trim)
                .filter(line -> line.startsWith("at "))
                .findFirst()
                .orElse("");
    }

    private static String value(Object value) {
        return value == null ? "" : value.toString();
    }
}
