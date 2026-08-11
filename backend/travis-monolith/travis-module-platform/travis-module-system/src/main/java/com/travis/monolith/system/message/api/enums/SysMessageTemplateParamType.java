package com.travis.monolith.system.message.api.enums;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 消息模板参数类型枚举 */
@Getter
@AllArgsConstructor
public enum SysMessageTemplateParamType {
    /** 文本 */
    TEXT("text"),

    /** 数字 */
    NUMBER("number"),

    /** 金额 */
    AMOUNT("amount"),

    /** 日期 */
    DATE("date"),

    /** 日期时间 */
    DATETIME("datetime"),

    /** 手机号 */
    MOBILE("mobile"),

    /** 邮箱 */
    EMAIL("email"),

    /** 链接 */
    URL("url");

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 金额参数格式，最多保留两位小数 */
    private static final String AMOUNT_PATTERN = "^-?\\d+(\\.\\d{1,2})?$";

    /** 邮箱参数格式 */
    private static final String EMAIL_PATTERN = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";

    /** 中国大陆手机号参数格式 */
    private static final String MOBILE_PATTERN = "^1[3-9]\\d{9}$";

    /** 普通数字参数格式 */
    private static final String NUMBER_PATTERN = "^-?\\d+(\\.\\d+)?$";

    /** HTTP 或 HTTPS 地址参数格式 */
    private static final String URL_PATTERN = "^https?://\\S+$";

    private static final Set<String> VALUES =
            Arrays.stream(values())
                    .map(SysMessageTemplateParamType::getValue)
                    .collect(Collectors.toSet());

    /** 参数类型值 */
    private final String value;

    public static boolean contains(String value) {
        return VALUES.contains(value);
    }

    public static SysMessageTemplateParamType of(String value) {
        return Arrays.stream(values())
                .filter(item -> item.value.equals(value))
                .findFirst()
                .orElse(null);
    }

    public boolean validate(String value) {
        if (value == null || value.isBlank() || this == TEXT) {
            return true;
        }
        try {
            switch (this) {
                case NUMBER -> {
                    return value.matches(NUMBER_PATTERN);
                }
                case AMOUNT -> {
                    return value.matches(AMOUNT_PATTERN);
                }
                case DATE -> LocalDate.parse(value);
                case DATETIME -> LocalDateTime.parse(value, DATE_TIME_FORMATTER);
                case MOBILE -> {
                    return value.matches(MOBILE_PATTERN);
                }
                case EMAIL -> {
                    return value.matches(EMAIL_PATTERN);
                }
                case URL -> {
                    return value.matches(URL_PATTERN);
                }
                default -> {
                    return true;
                }
            }
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    public String getInvalidMessage() {
        return switch (this) {
            case NUMBER -> "请输入合法数字";
            case AMOUNT -> "请输入合法金额，最多保留2位小数";
            case DATE -> "请输入合法日期，格式为YYYY-MM-DD";
            case DATETIME -> "请输入合法日期时间，格式为YYYY-MM-DD HH:mm:ss";
            case MOBILE -> "请输入合法手机号";
            case EMAIL -> "请输入合法邮箱";
            case URL -> "请输入以 http:// 或 https:// 开头的链接";
            default -> "参数格式错误";
        };
    }
}
