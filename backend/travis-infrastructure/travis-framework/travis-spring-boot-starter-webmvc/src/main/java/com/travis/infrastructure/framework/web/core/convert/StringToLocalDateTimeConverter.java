package com.travis.infrastructure.framework.web.core.convert;

import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * Query/Form 参数中的 LocalDateTime 转换器。
 *
 * @author travis
 */
public class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {

    private static final Pattern ISO_OFFSET_PATTERN =
            Pattern.compile(
                    "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:\\.\\d{1,9})?(?:Z|[+-]\\d{2}:?\\d{2})$");

    private static final Pattern ISO_LOCAL_PATTERN =
            Pattern.compile("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:\\.\\d{1,9})?$");

    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private final DateTimeFormatter defaultFormatter;
    private final Pattern defaultPattern;

    public StringToLocalDateTimeConverter(String defaultDateFormat) {
        String dateFormat =
                StringUtils.hasText(defaultDateFormat) ? defaultDateFormat : DEFAULT_DATE_FORMAT;
        this.defaultFormatter = DateTimeFormatter.ofPattern(dateFormat);
        this.defaultPattern = Pattern.compile("^" + toRegex(dateFormat) + "$");
    }

    @Override
    public LocalDateTime convert(@NonNull String source) {
        if (!StringUtils.hasText(source)) {
            return null;
        }
        String value = source.trim();
        if (ISO_OFFSET_PATTERN.matcher(value).matches()) {
            return OffsetDateTime.parse(
                            normalizeOffset(value), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    .withOffsetSameInstant(ZoneOffset.UTC)
                    .toLocalDateTime();
        }
        if (ISO_LOCAL_PATTERN.matcher(value).matches()) {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        if (defaultPattern.matcher(value).matches()) {
            return LocalDateTime.parse(value, defaultFormatter);
        }
        throw new IllegalArgumentException("Unsupported LocalDateTime format: " + source);
    }

    private static String normalizeOffset(String value) {
        if (value.endsWith("Z")) {
            return value;
        }
        int offsetStart = Math.max(value.lastIndexOf('+'), value.lastIndexOf('-'));
        if (offsetStart > 0 && value.length() - offsetStart == 5) {
            return value.substring(0, offsetStart + 3)
                    + ":"
                    + value.substring(offsetStart + 3);
        }
        return value;
    }

    private static String toRegex(String dateFormat) {
        StringBuilder regex = new StringBuilder();
        int index = 0;
        while (index < dateFormat.length()) {
            char ch = dateFormat.charAt(index);
            int end = index + 1;
            while (end < dateFormat.length() && dateFormat.charAt(end) == ch) {
                end++;
            }
            int count = end - index;
            regex.append(switch (ch) {
                case 'y', 'M', 'd', 'H', 'm', 's' -> "\\d{" + count + "}";
                case 'S' -> "\\d{1," + count + "}";
                default -> Pattern.quote(dateFormat.substring(index, end));
            });
            index = end;
        }
        return regex.toString();
    }
}
