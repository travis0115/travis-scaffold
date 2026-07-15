package com.travis.infrastructure.framework.quartz.core;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Quartz 任务处理器白名单。 */
public class QuartzJobHandlerRegistry {

    private final Map<String, QuartzJobHandler> handlers;

    public QuartzJobHandlerRegistry(List<QuartzJobHandler> handlers) {
        Map<String, QuartzJobHandler> registry = new LinkedHashMap<>();
        for (QuartzJobHandler handler : handlers) {
            String name = handler.getName();
            if (name == null || name.isBlank()) {
                throw new IllegalStateException("QuartzJobHandler 名称不能为空");
            }
            if (registry.putIfAbsent(name, handler) != null) {
                throw new IllegalStateException("QuartzJobHandler 名称重复: " + name);
            }
        }
        this.handlers = Map.copyOf(registry);
    }

    /** 根据名称获取任务处理器，不存在时抛出异常。 */
    public QuartzJobHandler getRequired(String name) {
        QuartzJobHandler handler = handlers.get(name);
        if (handler == null) {
            throw new IllegalStateException("未注册 QuartzJobHandler: " + name);
        }
        return handler;
    }

    /** 判断是否已注册指定名称的任务处理器。 */
    public boolean contains(String name) {
        return handlers.containsKey(name);
    }

    /** 获取全部已注册任务处理器名称。 */
    public Collection<String> names() {
        return handlers.keySet();
    }
}
