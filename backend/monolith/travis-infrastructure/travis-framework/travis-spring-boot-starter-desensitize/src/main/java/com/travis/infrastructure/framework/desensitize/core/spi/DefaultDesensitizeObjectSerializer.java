package com.travis.infrastructure.framework.desensitize.core.spi;

import lombok.AllArgsConstructor;
import tools.jackson.databind.ObjectMapper;

/**
 * 基于 Jackson ObjectMapper 的脱敏序列化实现
 * ObjectMapper 已注册 DesensitizeJacksonModule，序列化时自动触发脱敏
 */
@AllArgsConstructor
public class DefaultDesensitizeObjectSerializer implements DesensitizeObjectSerializer {

    private ObjectMapper objectMapper;

    @Override
    public String serialize(Object obj) {
        if (obj == null) return "{}";
        if (obj instanceof String s) return s;
        return objectMapper.writeValueAsString(obj);
    }
}