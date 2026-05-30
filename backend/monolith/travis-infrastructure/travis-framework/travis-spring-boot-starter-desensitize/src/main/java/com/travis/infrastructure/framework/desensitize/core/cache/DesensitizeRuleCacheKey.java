package com.travis.infrastructure.framework.desensitize.core.cache;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Objects;

public record DesensitizeRuleCacheKey(
        Class<? extends Annotation> annotationType,
        Map<String, Object> attributes
) {
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DesensitizeRuleCacheKey(Class<? extends Annotation> type, Map<String, Object> attributes1))) {
            return false;
        }

        return annotationType.equals(type)
                && Objects.equals(attributes, attributes1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(annotationType, attributes);
    }
}