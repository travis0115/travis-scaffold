package com.travis.infrastructure.framework.desensitize.core.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.travis.infrastructure.framework.desensitize.core.resolver.DesensitizeResolver;
import com.travis.infrastructure.framework.desensitize.core.rule.DesensitizeRule;
import com.travis.infrastructure.framework.desensitize.core.spi.DesensitizeObjectSerializer;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 统一脱敏 API
 * 即时用 toDesensitizedJson，日志延时用 ofDesensitizedJson
 */
@Slf4j
public class DesensitizeUtils {

    private static final Map<Class<?>, Map<String, DesensitizeRule>> FIELD_RULE_CACHE = new ConcurrentHashMap<>();

    /**
     * 从 Spring 容器懒加载并缓存，由容器中 DesensitizeObjectSerializer Bean 提供实现。
     * 容器未就绪或未注册该 Bean 时为 null，toDesensitizedJson 退化为 obj.toString()。
     */
    private static volatile DesensitizeObjectSerializer objectSerializer;

    private DesensitizeUtils() {
    }

    private static DesensitizeObjectSerializer getObjectSerializer() {
        var s = objectSerializer;
        if (s != null) return s;
        synchronized (DesensitizeUtils.class) {
            s = objectSerializer;
            if (s != null) return s;
            try {
                s = SpringUtil.getBean(DesensitizeObjectSerializer.class);
            } catch (Throwable t) {
                log.warn("未获取到 DesensitizeObjectSerializer", t);
            }
            objectSerializer = s;
            return s;
        }
    }

    /**
     * 将对象序列化为脱敏后的 JSON
     */
    public static String toDesensitizedJson(Object obj) {
        if (obj == null) return "null";
        var serializer = getObjectSerializer();
        if (serializer != null) return serializer.serialize(obj);
        return obj.toString();
    }

    /**
     * 对单个字符串按注解脱敏
     */
    public static String desensitize(String value, Annotation annotation) {
        if (StrUtil.isBlank(value) || annotation == null) return value;
        var rule = DesensitizeResolver.resolveRule(annotation);
        return rule != null ? rule.apply(value) : value;
    }

    /**
     * 对单个字符串按规则脱敏
     */
    public static String desensitize(String value, DesensitizeRule rule) {
        if (StrUtil.isBlank(value) || rule == null) return value;
        return rule.apply(value);
    }

    /**
     * 用注解类的默认值构建脱敏规则。
     * 例如 resolveDefaultRule(MobileDesensitize.class) → SliderRule(3,4,'*')
     */
    public static <A extends Annotation> DesensitizeRule resolveDefaultRule(Class<A> annotationType) {
        A proxy = (A) Proxy.newProxyInstance(
                annotationType.getClassLoader(),
                new Class[]{annotationType},
                (p, method, args) -> switch (method.getName()) {
                    case "annotationType" -> annotationType;
                    case "hashCode" -> System.identityHashCode(p);
                    case "equals" -> p == args[0];
                    case "toString" -> "@" + annotationType.getSimpleName() + "(defaults)";
                    default -> method.getDefaultValue();
                }
        );
        return DesensitizeResolver.resolveRule(proxy);
    }

    /**
     * 扫描类字段的脱敏注解，返回 fieldName → Rule 映射（缓存）
     */
    public static Map<String, DesensitizeRule> resolveFieldRules(Class<?> clazz) {
        return FIELD_RULE_CACHE.computeIfAbsent(clazz, DesensitizeUtils::doResolveFieldRules);
    }

    private static Map<String, DesensitizeRule> doResolveFieldRules(Class<?> clazz) {
        var rules = new HashMap<String, DesensitizeRule>();
        var current = clazz;
        while (current != null && current != Object.class) {
            for (var field : current.getDeclaredFields()) {
                if (rules.containsKey(field.getName())) continue;
                for (var annotation : field.getAnnotations()) {
                    var rule = DesensitizeResolver.resolveRule(annotation);
                    if (rule != null) {
                        rules.put(field.getName(), rule);
                        break;
                    }
                }
            }
            current = current.getSuperclass();
        }
        return rules.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(rules);
    }

    /**
     * 返回懒包装对象，用于日志占位符。仅在日志框架调用 toString() 时才会做脱敏序列化，级别关闭时零开销。
     * <p>示例：{@code log.info("用户: {}", DesensitizeUtils.ofDesensitizeJson(userVO));}
     */
    public static Object ofDesensitizedJson(Object obj) {
        return new LazyForLog(obj);
    }

    private record LazyForLog(Object target) {
        @Override
        public String toString() {
            return toDesensitizedJson(target);
        }
    }
}