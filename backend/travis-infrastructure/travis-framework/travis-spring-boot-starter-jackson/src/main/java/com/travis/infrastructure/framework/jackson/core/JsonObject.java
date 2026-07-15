package com.travis.infrastructure.framework.jackson.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * 可变 JSON 对象，接口对齐 fastjson/hutool 的 JSONObject。 底层为 Jackson {@link ObjectNode}，序列化使用 {@link
 * JsonUtil#getObjectMapper()}。
 */
public final class JsonObject {

    private final ObjectNode node;
    private final ObjectMapper mapper;

    public JsonObject() {
        this.mapper = JsonUtil.getObjectMapper();
        this.node = mapper.createObjectNode();
    }

    JsonObject(ObjectNode node, ObjectMapper mapper) {
        this.node = node;
        this.mapper = mapper;
    }

    /** 从 JSON 字符串解析，根须为对象；否则返回空 JsonObject */
    public static JsonObject of(String json) {
        if (json == null || json.isBlank()) {
            return new JsonObject();
        }
        var om = JsonUtil.getObjectMapper();
        try {
            var n = om.readTree(json);
            if (n != null && n.isObject()) {
                return new JsonObject((ObjectNode) n, om);
            }
        } catch (Exception ignored) {
            // 解析失败返回空
        }
        return new JsonObject();
    }

    /** 获取字符串值，字段不存在或为 null 时返回 {@code null}。 */
    public String getString(String key) {
        var v = node.path(key);
        return v.isMissingNode() || v.isNull() ? null : v.asString();
    }

    /** 获取整数值，字段不存在或为 null 时返回 {@code null}。 */
    public Integer getInteger(String key) {
        var v = node.path(key);
        if (v.isMissingNode() || v.isNull()) return null;
        return v.isInt() ? v.intValue() : v.asInt();
    }

    /** 获取长整数值，字段不存在或为 null 时返回 {@code null}。 */
    public Long getLong(String key) {
        var v = node.path(key);
        if (v.isMissingNode() || v.isNull()) return null;
        return v.isLong() ? v.longValue() : v.asLong();
    }

    /** 获取布尔值，字段不存在或为 null 时返回 {@code null}。 */
    public Boolean getBoolean(String key) {
        var v = node.path(key);
        if (v.isMissingNode() || v.isNull()) return null;
        return v.asBoolean();
    }

    /** 获取双精度数值，字段不存在或为 null 时返回 {@code null}。 */
    public Double getDouble(String key) {
        var v = node.path(key);
        if (v.isMissingNode() || v.isNull()) return null;
        return v.asDouble();
    }

    /** 获取高精度小数值，字段不存在或为 null 时返回 {@code null}。 */
    public BigDecimal getBigDecimal(String key) {
        var v = node.path(key);
        if (v.isMissingNode() || v.isNull()) return null;
        return v.decimalValue();
    }

    /** 获取高精度整数值，字段不存在或为 null 时返回 {@code null}。 */
    public BigInteger getBigInteger(String key) {
        var v = node.path(key);
        if (v.isMissingNode() || v.isNull()) return null;
        return v.bigIntegerValue();
    }

    /** 获取子对象（只读包装），若不存在或非对象则返回 null */
    public JsonObject getObject(String key) {
        var v = node.path(key);
        if (v.isMissingNode() || !v.isObject()) return null;
        return new JsonObject((ObjectNode) v, mapper);
    }

    /** 获取数组包装，若不存在或非数组则返回 null */
    public JsonArray getArray(String key) {
        var v = node.path(key);
        if (v.isMissingNode() || !v.isArray()) return null;
        return new JsonArray((ArrayNode) v, mapper);
    }

    /** 按类型获取并转为 Bean */
    public <T> T get(String key, Class<T> clazz) {
        var v = node.path(key);
        if (v.isMissingNode() || v.isNull()) return null;
        try {
            return mapper.treeToValue(v, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 写入任意值（null/基本类型/String/Map/List/JsonObject/JsonArray 等） */
    public JsonObject put(String key, Object value) {
        switch (value) {
            case null -> node.set(key, node.nullNode());
            case JsonObject jo -> node.set(key, jo.node);
            case JsonArray ja -> node.set(key, ja.node);
            default -> node.set(key, mapper.valueToTree(value));
        }
        return this;
    }

    /** 判断对象是否包含指定字段。 */
    public boolean has(String key) {
        return node.has(key);
    }

    /** 删除指定字段并返回当前对象，便于链式调用。 */
    public JsonObject remove(String key) {
        node.remove(key);
        return this;
    }

    /** 将当前对象序列化为 JSON 字符串。 */
    public String toJsonString() {
        try {
            return mapper.writeValueAsString(node);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 转为指定类型 Bean */
    public <T> T toBean(Class<T> clazz) {
        try {
            return mapper.treeToValue(node, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 暴露底层 ObjectNode，便于与 Jackson 生态互操作 */
    public ObjectNode unwrap() {
        return node;
    }
}
