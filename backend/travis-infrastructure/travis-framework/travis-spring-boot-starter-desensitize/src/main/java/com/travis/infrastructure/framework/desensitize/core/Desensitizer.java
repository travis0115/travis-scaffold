package com.travis.infrastructure.framework.desensitize.core;

import cn.hutool.core.util.StrUtil;
import com.travis.infrastructure.framework.desensitize.core.resolver.DesensitizeResolver;
import com.travis.infrastructure.framework.desensitize.core.rule.DesensitizeRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;

/** 统一处理字符串、对象、参数和 JSON 的脱敏，失败时返回空结果，避免影响调用方业务。 */
@Slf4j
@RequiredArgsConstructor
public class Desensitizer {

    private static final String MASKED_VALUE = "******";
    private static final Set<String> DEFAULT_SENSITIVE_FIELDS =
            Set.of(
                    "password",
                    "oldpassword",
                    "newpassword",
                    "confirmpassword",
                    "pwd",
                    "secret",
                    "secretkey",
                    "accesskey",
                    "token",
                    "accesstoken",
                    "refreshtoken",
                    "authorization",
                    "credential",
                    "credentials",
                    "privatekey");

    private final ObjectMapper objectMapper;

    public String desensitize(String value, Annotation annotation) {
        if (StrUtil.isBlank(value) || annotation == null) {
            return value;
        }
        try {
            return desensitize(value, DesensitizeResolver.resolveRule(annotation));
        } catch (Exception exception) {
            log.warn("字符串脱敏失败，已放弃记录", exception);
            return null;
        }
    }

    public String desensitize(String value, DesensitizeRule rule) {
        if (StrUtil.isBlank(value) || rule == null) {
            return value;
        }
        try {
            return rule.apply(value);
        } catch (Exception exception) {
            log.warn("字符串脱敏失败，已放弃记录", exception);
            return null;
        }
    }

    public String desensitizeObject(Object value) {
        return desensitizeObject(value, Collections.emptySet());
    }

    public String desensitizeObject(Object value, Set<String> additionalSensitiveFields) {
        if (value == null) {
            return null;
        }
        if (value instanceof String string) {
            return string;
        }
        try {
            var root = objectMapper.valueToTree(value);
            maskSensitiveFields(root, sensitiveFields(additionalSensitiveFields));
            return objectMapper.writeValueAsString(root);
        } catch (Exception exception) {
            log.warn("对象脱敏失败，已放弃记录", exception);
            return null;
        }
    }

    public Map<String, String> desensitizeParameters(
            Map<String, String> parameters, MethodParameter[] methodParameters) {
        return desensitizeParameters(parameters, methodParameters, Collections.emptySet());
    }

    /** 调用方提供的字段名会追加到默认敏感字段集合中。 */
    public Map<String, String> desensitizeParameters(
            Map<String, String> parameters,
            MethodParameter[] methodParameters,
            Set<String> additionalSensitiveFields) {
        if (parameters == null || parameters.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            var result = new LinkedHashMap<>(parameters);
            if (methodParameters != null) {
                for (var parameter : methodParameters) {
                    var parameterType = parameter.getParameterType();
                    if (BeanUtils.isSimpleValueType(parameterType)) {
                        desensitizeSimpleParameter(parameter, result);
                    } else if (isUserDefinedType(parameterType)) {
                        DesensitizeResolver.resolveFieldRules(parameterType)
                                .forEach((key, rule) -> applyRule(result, key, rule));
                    }
                }
            }
            var sensitiveFields = sensitiveFields(additionalSensitiveFields);
            result.replaceAll(
                    (key, value) ->
                            sensitiveFields.contains(normalizeFieldName(key))
                                    ? MASKED_VALUE
                                    : value);
            return result;
        } catch (Exception exception) {
            log.warn("请求参数脱敏失败，已放弃记录", exception);
            return Collections.emptyMap();
        }
    }

    public String desensitizeJson(String json, Type targetType) {
        return desensitizeJson(json, targetType, Collections.emptySet());
    }

    /** 先应用对象注解规则，再按默认及调用方追加的字段名递归兜底脱敏。 */
    public String desensitizeJson(
            String json, Type targetType, Set<String> additionalSensitiveFields) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            JsonNode root;
            if (targetType == null) {
                root = objectMapper.readTree(json);
            } else {
                var target =
                        objectMapper.readValue(
                                json, objectMapper.getTypeFactory().constructType(targetType));
                root = target == null ? null : objectMapper.valueToTree(target);
            }
            if (root == null) {
                return null;
            }
            maskSensitiveFields(root, sensitiveFields(additionalSensitiveFields));
            return objectMapper.writeValueAsString(root);
        } catch (Exception exception) {
            log.warn("JSON 脱敏失败，已放弃记录", exception);
            return null;
        }
    }

    private void desensitizeSimpleParameter(MethodParameter parameter, Map<String, String> result) {
        var parameterName = parameter.getParameterName();
        if (parameterName == null || !result.containsKey(parameterName)) {
            return;
        }
        for (var annotation : parameter.getParameterAnnotations()) {
            var rule = DesensitizeResolver.resolveRule(annotation);
            if (rule != null) {
                applyRule(result, parameterName, rule);
                return;
            }
        }
    }

    private void applyRule(Map<String, String> parameters, String key, DesensitizeRule rule) {
        parameters.computeIfPresent(key, (_, value) -> desensitize(value, rule));
    }

    private void maskSensitiveFields(JsonNode node, Set<String> sensitiveFields) {
        if (node instanceof ObjectNode objectNode) {
            for (var entry : new ArrayList<>(objectNode.properties())) {
                if (sensitiveFields.contains(normalizeFieldName(entry.getKey()))) {
                    objectNode.put(entry.getKey(), MASKED_VALUE);
                } else {
                    maskSensitiveFields(entry.getValue(), sensitiveFields);
                }
            }
        } else if (node instanceof ArrayNode arrayNode) {
            arrayNode.forEach(child -> maskSensitiveFields(child, sensitiveFields));
        }
    }

    private Set<String> sensitiveFields(Set<String> additionalSensitiveFields) {
        if (additionalSensitiveFields == null || additionalSensitiveFields.isEmpty()) {
            return DEFAULT_SENSITIVE_FIELDS;
        }
        var fields = new HashSet<>(DEFAULT_SENSITIVE_FIELDS);
        additionalSensitiveFields.stream()
                .filter(field -> field != null && !field.isBlank())
                .map(this::normalizeFieldName)
                .forEach(fields::add);
        return fields;
    }

    private boolean isUserDefinedType(Class<?> type) {
        var name = type.getName();
        return !name.startsWith("java.")
                && !name.startsWith("javax.")
                && !name.startsWith("jakarta.")
                && !name.startsWith("org.springframework.");
    }

    private String normalizeFieldName(String fieldName) {
        return fieldName.replace("_", "").replace("-", "").toLowerCase(Locale.ROOT);
    }
}
