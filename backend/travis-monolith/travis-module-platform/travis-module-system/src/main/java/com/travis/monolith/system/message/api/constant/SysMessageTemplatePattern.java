package com.travis.monolith.system.message.api.constant;

import java.util.regex.Pattern;

/** 消息模板正则表达式。 */
public final class SysMessageTemplatePattern {
    private static final String PARAM_KEY_REGEX = "[A-Za-z][A-Za-z0-9_]*";

    /** 合法模板参数键格式。 */
    public static final Pattern PARAM_KEY_PATTERN = Pattern.compile("^" + PARAM_KEY_REGEX + "$");

    /** 从模板正文中提取双花括号参数的表达式。 */
    public static final Pattern TEMPLATE_PARAM_EXPRESSION_PATTERN =
            Pattern.compile("\\{\\{\\s*([^{}]+?)\\s*}}");

    /** 匹配可渲染的合法模板变量。 */
    public static final Pattern TEMPLATE_VARIABLE_PATTERN =
            Pattern.compile("\\{\\{\\s*(" + PARAM_KEY_REGEX + ")\\s*}}");

    private SysMessageTemplatePattern() {}
}
