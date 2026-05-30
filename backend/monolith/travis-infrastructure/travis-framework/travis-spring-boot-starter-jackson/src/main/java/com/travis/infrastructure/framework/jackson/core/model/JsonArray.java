package com.travis.infrastructure.framework.jackson.core.model;

import com.travis.infrastructure.framework.jackson.core.util.JsonUtils;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 只读 JSON 数组视图，接口对齐 fastjson/hutool 的 JSONArray 常用读操作。
 */
public final class JsonArray {

    final ArrayNode node;
    private final ObjectMapper mapper;

    JsonArray() {
        this.mapper = JsonUtils.getObjectMapper();
        this.node = mapper.createArrayNode();
    }

    JsonArray(ArrayNode node, ObjectMapper mapper) {
        this.node = node;
        this.mapper = mapper;
    }

    public int size() {
        return node.size();
    }

    public String getString(int index) {
        var v = node.path(index);
        return v.isMissingNode() || v.isNull() ? null : v.asString();
    }

    public Integer getInteger(int index) {
        var v = node.path(index);
        if (v.isMissingNode() || v.isNull()) return null;
        return v.asInt();
    }

    public Long getLong(int index) {
        var v = node.path(index);
        if (v.isMissingNode() || v.isNull()) return null;
        return v.asLong();
    }

    public Boolean getBoolean(int index) {
        var v = node.path(index);
        if (v.isMissingNode() || v.isNull()) return null;
        return v.asBoolean();
    }

    public JsonObject getObject(int index) {
        var v = node.path(index);
        if (v.isMissingNode() || !v.isObject()) return null;
        return new JsonObject((ObjectNode) v, mapper);
    }

    public JsonArray getArray(int index) {
        var v = node.path(index);
        if (v.isMissingNode() || !v.isArray()) return null;
        return new JsonArray((ArrayNode) v, mapper);
    }

    public <T> T get(int index, Class<T> clazz) {
        var v = node.path(index);
        if (v.isMissingNode() || v.isNull()) return null;
        try {
            return mapper.treeToValue(v, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> toList(Class<T> elementClass) {
        var list = new ArrayList<T>(node.size());
        for (int i = 0; i < node.size(); i++) {
            list.add(get(i, elementClass));
        }
        return list;
    }

    public String toJsonString() {
        try {
            return mapper.writeValueAsString(node);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayNode unwrap() {
        return node;
    }
}
