package com.travis.infrastructure.framework.desensitize.core.spi;

/**
 * 对象脱敏序列化 SPI
 * 将任意对象转为脱敏后的字符串表示（通常是 JSON）
 * 由各桥接模块提供实现（如 Jackson 模块注册基于 ObjectMapper 的实现）
 */
public interface DesensitizeObjectSerializer {
    String serialize(Object obj);
}