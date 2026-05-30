package com.travis.infrastructure.framework.desensitize.core.rule;

/**
 * 脱敏规则接口
 */
public interface DesensitizeRule {
    String apply(String value);
}