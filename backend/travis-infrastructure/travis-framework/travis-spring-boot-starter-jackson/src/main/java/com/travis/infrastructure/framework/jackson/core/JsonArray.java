package com.travis.infrastructure.framework.jackson.core;

import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/** 只读 JSON 数组视图，接口对齐 fastjson/hutool 的 JSONArray 常用读操作。 */
public final class JsonArray {

    final ArrayNode node;
    private final ObjectMapper mapper;

    JsonArray() {
        this.mapper = JsonUtil.getObjectMapper();
        this.node = mapper.createArrayNode();
    }

    JsonArray(ArrayNode node, ObjectMapper mapper) {
        this.node = node;
        this.mapper = mapper;
    }

    /** 获取数组元素数量。 */
    public int size() {
        return node.size();
    }

    /** 获取指定下标的字符串值。 */
    public String getString(int index) {
        var v = node.path(index);
        return v.isMissingNode() || v.isNull() ? null : v.asString();
    }

    /** 获取指定下标的整数值。 */
    public Integer getInteger(int index) {
        var v = node.path(index);
        if (v.isMissingNode() || v.isNull()) return null;
        return v.asInt();
    }

    /** 获取指定下标的长整数值。 */
    public Long getLong(int index) {
        var v = node.path(index);
        if (v.isMissingNode() || v.isNull()) return null;
        return v.asLong();
    }

    /** 获取指定下标的布尔值。 */
    public Boolean getBoolean(int index) {
        var v = node.path(index);
        if (v.isMissingNode() || v.isNull()) return null;
        return v.asBoolean();
    }

    /** 获取指定下标的 JSON 对象包装。 */
    public JsonObject getObject(int index) {
        var v = node.path(index);
        if (v.isMissingNode() || !v.isObject()) return null;
        return new JsonObject((ObjectNode) v, mapper);
    }

    /** 获取指定下标的 JSON 数组包装。 */
    public JsonArray getArray(int index) {
        var v = node.path(index);
        if (v.isMissingNode() || !v.isArray()) return null;
        return new JsonArray((ArrayNode) v, mapper);
    }

    /** 将指定下标的值转换为目标类型。 */
    public <T> T get(int index, Class<T> clazz) {
        var v = node.path(index);
        if (v.isMissingNode() || v.isNull()) return null;
        try {
            return mapper.treeToValue(v, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 将数组元素转换为指定类型的列表。 */
    public <T> List<T> toList(Class<T> elementClass) {
        var list = new ArrayList<T>(node.size());
        for (int i = 0; i < node.size(); i++) {
            list.add(get(i, elementClass));
        }
        return list;
    }

    /** 将当前数组序列化为 JSON 字符串。 */
    public String toJsonString() {
        try {
            return mapper.writeValueAsString(node);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 暴露底层 ArrayNode，便于与 Jackson 生态互操作。 */
    public ArrayNode unwrap() {
        return node;
    }
}
