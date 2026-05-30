package com.travis.infrastructure.framework.desensitize.core.rule;

/**
 * 滑动脱敏规则
 * @param prefix
 * @param suffix
 * @param mask
 */
public record SliderDesensitizeRule(int prefix, int suffix, char mask) implements DesensitizeRule {

    @Override
    public String apply(String value) {
        if (value == null) return null;
        if (value.length() <= prefix + suffix || prefix < 0 || suffix < 0) {
            return value;
        }
        var builder = new StringBuilder(value.length());
        builder.append(value, 0, prefix);
        builder.append(String.valueOf(mask).repeat(value.length() - prefix - suffix));
        builder.append(value.substring(value.length() - suffix));
        return builder.toString();
    }
}