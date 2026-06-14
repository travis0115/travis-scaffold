package com.travis.monolith.ops.job.internal.service;

import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.framework.jackson.core.JsonUtil;
import com.travis.monolith.ops.job.api.OpsJobErrorCode;
import tools.jackson.databind.JsonNode;

import java.util.Iterator;
import java.util.Map;

/** 校验 JSON 参数及项目当前支持的 JSON Schema 常用子集。 */
public final class OpsJobParamValidator {

    private OpsJobParamValidator() {}

    public static void validate(String params, String schema) {
        JsonNode paramsNode = parseJson(params, "参数必须是合法 JSON");
        if (schema == null || schema.isBlank()) {
            return;
        }
        JsonNode schemaNode = parseJson(schema, "JSON Schema 必须是合法 JSON");
        if (!schemaNode.isObject()) {
            throw invalid("JSON Schema 根节点必须是对象");
        }
        validateNode(paramsNode, schemaNode, "$");
    }

    private static JsonNode parseJson(String value, String message) {
        try {
            return value == null || value.isBlank()
                    ? JsonUtil.getObjectMapper().readTree("{}")
                    : JsonUtil.getObjectMapper().readTree(value);
        } catch (Exception exception) {
            throw invalid(message);
        }
    }

    private static void validateNode(
            JsonNode value, JsonNode schema, String path) {
        String type = schema.path("type").asString("");
        if (!type.isBlank() && !matchesType(value, type)) {
            throw invalid(path + " 类型必须为 " + type);
        }
        if (value.isObject()) {
            for (JsonNode required : schema.path("required")) {
                String field = required.asString();
                if (!value.has(field) || value.path(field).isNull()) {
                    throw invalid(path + "." + field + " 为必填字段");
                }
            }
        }
        JsonNode properties = schema.path("properties");
        if (value.isObject() && properties.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = properties.properties().iterator();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (value.has(field.getKey())) {
                    validateNode(
                            value.path(field.getKey()),
                            field.getValue(),
                            path + "." + field.getKey()
                    );
                }
            }
        }
        if (value.isArray() && schema.has("items")) {
            for (int index = 0; index < value.size(); index++) {
                validateNode(
                        value.get(index), schema.path("items"), path + "[" + index + "]");
            }
        }
    }

    private static boolean matchesType(JsonNode value, String type) {
        return switch (type) {
            case "object" -> value.isObject();
            case "array" -> value.isArray();
            case "string" -> value.isString();
            case "integer" -> value.isIntegralNumber();
            case "number" -> value.isNumber();
            case "boolean" -> value.isBoolean();
            case "null" -> value.isNull();
            default -> true;
        };
    }

    private static BizException invalid(String message) {
        return new BizException(OpsJobErrorCode.INVALID_PARAMS, message);
    }
}
