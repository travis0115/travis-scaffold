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

    public QuartzJobHandler getRequired(String name) {
        QuartzJobHandler handler = handlers.get(name);
        if (handler == null) {
            throw new IllegalStateException("未注册 QuartzJobHandler: " + name);
        }
        return handler;
    }

    public boolean contains(String name) {
        return handlers.containsKey(name);
    }

    public Collection<String> names() {
        return handlers.keySet();
    }
}
