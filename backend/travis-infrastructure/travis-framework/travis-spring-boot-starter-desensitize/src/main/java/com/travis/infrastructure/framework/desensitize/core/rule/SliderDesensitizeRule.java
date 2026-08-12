package com.travis.infrastructure.framework.desensitize.core.rule;

/**
 * 滑动脱敏规则
 *
 * @param prefix 保留的前缀字符数
 * @param suffix 保留的后缀字符数
 * @param mask 中间内容使用的掩码字符
 */
public record SliderDesensitizeRule(int prefix, int suffix, char mask) implements DesensitizeRule {

    @Override
    public String apply(String value) {
        if (value == null) return null;
        if (value.length() <= prefix + suffix || prefix < 0 || suffix < 0) {
            return value;
        }
        return value.substring(0, prefix) +
                String.valueOf(mask).repeat(value.length() - prefix - suffix) +
                value.substring(value.length() - suffix);
    }
}
