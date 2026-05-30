package com.travis.infrastructure.framework.desensitize.core.cache;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 方法句柄缓存
 */
public final class DesensitizeMethodHandleCache {

    /**
     * 外层缓存：annotationType -> 内层 methodName -> MethodHandle
     */
    private static final Map<Class<?>, Map<String, MethodHandle>> METHOD_HANDLE_CACHE = new ConcurrentHashMap<>();

    /**
     * 只创建一次 Lookup
     */
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();

    private DesensitizeMethodHandleCache() {
    }

    /**
     * 获取任意注解字段的 MethodHandle
     */
    public static MethodHandle getHandle(Class<?> annotationType, String methodName) {
        // 先拿 annotationType 对应的 methodMap
        var methodMap = METHOD_HANDLE_CACHE.computeIfAbsent(annotationType, _ -> new ConcurrentHashMap<>());

        // 再拿具体 methodName
        return methodMap.computeIfAbsent(methodName, name -> buildHandle(annotationType, name)
        );
    }

    private static MethodHandle buildHandle(Class<?> annotationType, String methodName) {
        try {
            var method = annotationType.getMethod(methodName);
            return LOOKUP.unreflect(method);
        } catch (NoSuchMethodException e) {
            return null;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
