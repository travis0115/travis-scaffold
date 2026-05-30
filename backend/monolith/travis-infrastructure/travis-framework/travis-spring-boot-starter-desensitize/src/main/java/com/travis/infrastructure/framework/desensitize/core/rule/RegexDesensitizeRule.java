package com.travis.infrastructure.framework.desensitize.core.rule;

import cn.hutool.core.util.StrUtil;

import java.util.regex.Pattern;

/**
 * 正则脱敏规则
 *
 * @param pattern
 * @param replacer
 */
public record RegexDesensitizeRule(Pattern pattern, String replacer) implements DesensitizeRule {

    public RegexDesensitizeRule(String regex, String replacer) {
        this(compile(regex, null), replacer);
    }

    @Override
    public String apply(String value) {
        if (value == null) return null;
        if (StrUtil.isBlank(value)) return "";
        return pattern.matcher(value).replaceAll(replacer);
    }

    /**
     * 供 Resolver 调用，便于在异常中带上注解信息
     */
    public static RegexDesensitizeRule of(String regex, String replacer, String annotationSource) {
        return new RegexDesensitizeRule(compile(regex, annotationSource), replacer);
    }

    private static Pattern compile(String regex, String annotationSource) {
        try {
            return Pattern.compile(regex);
        } catch (Exception e) {
            var msg = "Invalid desensitize regex: \"" + regex + "\"";
            if (annotationSource != null) {
                msg += " (from " + annotationSource + ")";
            }
            throw new IllegalArgumentException(msg, e);
        }
    }
}