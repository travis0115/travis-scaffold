package com.travis.infrastructure.framework.desensitize.core.cache;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Objects;

/** 脱敏规则缓存键，由注解类型及其属性共同确定。 */
public record DesensitizeRuleCacheKey(
        /** 脱敏注解类型。 */
        Class<? extends Annotation> annotationType,
        /** 参与规则计算的注解属性。 */
        Map<String, Object> attributes) {
    @Override
    public boolean equals(Object o) {
        if (!(o
                instanceof
                DesensitizeRuleCacheKey(
                        Class<? extends Annotation> type,
                        Map<String, Object> attributes1))) {
            return false;
        }

        return annotationType.equals(type) && Objects.equals(attributes, attributes1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(annotationType, attributes);
    }
}
