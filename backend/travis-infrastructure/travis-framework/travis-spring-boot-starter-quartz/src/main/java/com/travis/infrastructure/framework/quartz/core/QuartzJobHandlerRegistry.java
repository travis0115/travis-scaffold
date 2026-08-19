package com.travis.infrastructure.framework.quartz.core;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;

/** Quartz 任务处理器白名单。 */
public class QuartzJobHandlerRegistry implements SmartInitializingSingleton {

    private final ObjectProvider<QuartzJobHandler> handlerProvider;
    private volatile Map<String, QuartzJobHandler> handlers;

    public QuartzJobHandlerRegistry(List<QuartzJobHandler> handlers) {
        this.handlerProvider = null;
        this.handlers = buildRegistry(handlers);
    }

    public QuartzJobHandlerRegistry(ObjectProvider<QuartzJobHandler> handlerProvider) {
        this.handlerProvider = handlerProvider;
        this.handlers = Map.of();
    }

    /** 等全部单例 Bean 创建完成后再解析处理器，避免处理器依赖任务服务时形成构造循环。 */
    @Override
    public void afterSingletonsInstantiated() {
        if (handlerProvider != null) {
            handlers = buildRegistry(handlerProvider.orderedStream().toList());
        }
    }

    private Map<String, QuartzJobHandler> buildRegistry(List<QuartzJobHandler> handlers) {
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
        return Map.copyOf(registry);
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

    /** 判断指定处理器是否仅供系统内置任务使用。 */
    public boolean isBuiltin(String name) {
        QuartzJobHandler handler = handlers.get(name);
        return handler != null && handler.isBuiltin();
    }

    /** 获取可由管理员创建任务的处理器名称。 */
    public Collection<String> names() {
        return descriptors().stream().map(QuartzJobHandlerDescriptor::name).toList();
    }

    /** 获取可由管理员创建任务的处理器名称及说明。 */
    public Collection<QuartzJobHandlerDescriptor> descriptors() {
        return descriptors(false);
    }

    /** 获取任务处理器名称及说明，可按需包含系统内置处理器。 */
    public Collection<QuartzJobHandlerDescriptor> descriptors(boolean includeBuiltin) {
        return handlers.values().stream()
                .filter(handler -> includeBuiltin || !handler.isBuiltin())
                .map(
                        handler ->
                                new QuartzJobHandlerDescriptor(
                                        handler.getName(), handler.getDescription()))
                .sorted(Comparator.comparing(QuartzJobHandlerDescriptor::name))
                .toList();
    }
}
