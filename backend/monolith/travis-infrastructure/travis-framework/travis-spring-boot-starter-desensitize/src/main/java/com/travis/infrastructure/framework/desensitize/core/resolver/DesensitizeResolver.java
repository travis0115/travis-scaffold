package com.travis.infrastructure.framework.desensitize.core.resolver;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.travis.infrastructure.framework.desensitize.core.annotation.RegexDesensitize;
import com.travis.infrastructure.framework.desensitize.core.annotation.SliderDesensitize;
import com.travis.infrastructure.framework.desensitize.core.cache.AnnotationAttributeSnapshotBuilder;
import com.travis.infrastructure.framework.desensitize.core.cache.DesensitizeMethodHandleCache;
import com.travis.infrastructure.framework.desensitize.core.cache.DesensitizeRuleCacheKey;
import com.travis.infrastructure.framework.desensitize.core.rule.DesensitizeRule;
import com.travis.infrastructure.framework.desensitize.core.rule.RegexDesensitizeRule;
import com.travis.infrastructure.framework.desensitize.core.rule.SliderDesensitizeRule;
import com.travis.infrastructure.framework.desensitize.core.spel.EvaluationContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 脱敏解析器
 */
@Slf4j
public class DesensitizeResolver {

    /**
     * SpEL 解析器
     */
    private static final ExpressionParser PARSER = new SpelExpressionParser();


    /**
     * 规则缓存（支持字段参数覆盖）
     */
    private static final Map<DesensitizeRuleCacheKey, DesensitizeRule> RULE_CACHE = new ConcurrentHashMap<>();

    /**
     * SpEL 缓存
     */
    private static final Map<String, Expression> SPEL_CACHE = new ConcurrentHashMap<>();

    /**
     * SpEL 上下文
     * 非 Spring 环境或容器未就绪：降级为无 SpEL，所有 disable 不生效
     */
    private static class ProviderHolder {
        private static final EvaluationContextProvider INSTANCE = getProviderOrNull();

        private static EvaluationContextProvider getProviderOrNull() {
            try {
                return SpringUtil.getBean(EvaluationContextProvider.class);
            } catch (Throwable t) {
                log.debug("EvaluationContextProvider not available (non-Spring or context not ready), disable " +
                        "expressions will be ignored: {}", t.getMessage());
                return null;
            }
        }
    }

    private DesensitizeResolver() {
    }

    /**
     * 解析规则入口
     */
    public static DesensitizeRule resolveRule(Annotation annotation) {
        if (annotation == null) {
            return null;
        }
        var key = new DesensitizeRuleCacheKey(annotation.annotationType(),
                AnnotationAttributeSnapshotBuilder.buildSnapshot(annotation,
                        Collections.newSetFromMap(new IdentityHashMap<>())));
        return RULE_CACHE.computeIfAbsent(key,
                _ -> resolveRuleDeep(annotation, Collections.newSetFromMap(new IdentityHashMap<>())
                ));
    }

    /**
     * 深度递归解析注解链，支持：
     * 1. 直接策略注解
     * 2. 业务注解使用策略注解作为元注解
     * 3. 多层元注解
     */
    private static DesensitizeRule resolveRuleDeep(Annotation annotation, Set<Class<?>> visited) {
        var type = annotation.annotationType();
        // 防止循环递归
        if (!visited.add(type)) {
            return null;
        }

        /*
         * 检查 disable
         */
        if (isDisabled(annotation)) {
            return null;
        }

        /*
         * 字段注解 RegexDesensitize
         */
        if (type == RegexDesensitize.class) {
            return buildRegexRule(annotation, (RegexDesensitize) annotation);
        }

        /*
         * 字段注解 SliderDesensitize
         */
        if (type == SliderDesensitize.class) {
            return buildSliderRule(annotation, (SliderDesensitize) annotation);
        }

        /*
         * 元注解 RegexDesensitize
         */
        var regexMeta = type.getAnnotation(RegexDesensitize.class);
        if (regexMeta != null) {
            return buildRegexRule(annotation, regexMeta);
        }

        /*
         * 元注解 SliderDesensitize
         */
        var sliderMeta = type.getAnnotation(SliderDesensitize.class);
        if (sliderMeta != null) {
            return buildSliderRule(annotation, sliderMeta);
        }

        /*
         * Recursive Search Meta Annotation
         */
        for (Annotation meta : type.getAnnotations()) {
            var rule = resolveRuleDeep(meta, visited);
            if (rule != null) {
                return rule;
            }
        }
        return null;
    }


    private static RegexDesensitizeRule buildRegexRule(Annotation sourceAnnotation, RegexDesensitize meta) {
        var regex = resolveAnnotationField(sourceAnnotation, "regex", meta.regex(), String.class);
        var replacer = resolveAnnotationField(sourceAnnotation, "replacer", meta.replacer(), String.class);

        return RegexDesensitizeRule.of(regex, replacer, sourceAnnotation.annotationType().getSimpleName());
    }


    private static SliderDesensitizeRule buildSliderRule(Annotation sourceAnnotation, SliderDesensitize meta) {
        var prefix = resolveAnnotationField(sourceAnnotation, "prefix", meta.prefix(), int.class);
        var suffix = resolveAnnotationField(sourceAnnotation, "suffix", meta.suffix(), int.class);
        var mask = resolveAnnotationField(sourceAnnotation, "mask", meta.mask(), char.class);
        return new SliderDesensitizeRule(prefix, suffix, mask);
    }

    private static <T> T resolveAnnotationField(Annotation annotation, String fieldName, T defaultValue,
                                                Class<T> type) {
        try {
            var handle = DesensitizeMethodHandleCache.getHandle(annotation.annotationType(), fieldName);
            if (handle == null) {
                return defaultValue;
            }
            var value = handle.invoke(annotation);
            if (value == null) {
                return defaultValue;
            }

            if (type == String.class) {
                if (StrUtil.isNotBlank((String) value)) {
                    return (T) value;
                }
                return defaultValue;
            }

            if (type == Integer.class || type == int.class) {
                int v = ((Number) value).intValue();
                if (v != -1) {
                    return (T) Integer.valueOf(v);
                }
                return defaultValue;
            }

            if (type == Character.class || type == char.class) {
                char v = (Character) value;
                if (v != 0) {
                    return (T) Character.valueOf(v);
                }
                return defaultValue;
            }

            return (T) value;

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Spel disable 统一解析
     * 若 EvaluationContextProvider 不可用（非 Spring 环境或容器未就绪），视为不禁用。
     */
    private static boolean isDisabled(Annotation annotation) {
        var handle = DesensitizeMethodHandleCache.getHandle(annotation.annotationType(), "disable");
        if (handle == null) {
            return false;
        }
        try {
            var expr = (String) handle.invoke(annotation);
            if (StrUtil.isBlank(expr)) {
                return false;
            }
            var provider = ProviderHolder.INSTANCE;
            if (provider == null) {
                return false;   // 降级：无法解析 SpEL 时不禁用
            }
            var expression = SPEL_CACHE.computeIfAbsent(expr, PARSER::parseExpression);
            var result = expression.getValue(provider.getContext(), Boolean.class);
            return Boolean.TRUE.equals(result);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


}
