package com.travis.infrastructure.framework.desensitize.core.cache;

import org.jspecify.annotations.NonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 注解属性快照构建器
 * 用于将注解的属性值提取到Map中，便于后续处理
 */
public final class AnnotationAttributeSnapshotBuilder {

    /**
     * 私构造函数，防止实例化
     */
    private AnnotationAttributeSnapshotBuilder() {
    }

    /**
     * 方法句柄缓存，按注解类型分类存储
     * 使用ClassValue确保每个类只计算一次，提高性能
     */
    private static final ClassValue<Map<String, MethodHandle>> HANDLE_CACHE =
            new ClassValue<>() {
                @Override
                protected Map<String, MethodHandle> computeValue(@NonNull Class<?> type) {
                    var map = new ConcurrentHashMap<String, MethodHandle>();
                    try {
                        var lookup = MethodHandles.lookup();
                        //注解类型的所有方法
                        for (var method : type.getMethods()) {
                            //只处理无参方法（注解属性访问器）
                            if (method.getParameterCount() != 0) {
                                continue;
                            }
                            //将方法名和对应的方法句柄存入缓存
                            map.put(method.getName(), lookup.unreflect(method));
                        }
                        return map;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };


    /**
     * 构建注解属性快照
     */
    public static Map<String, Object> buildSnapshot(Annotation annotation,
                                                    Set<Class<? extends Annotation>> visited) {
        var snapshot = new HashMap<String, Object>();
        var type = annotation.annotationType();
        if (!visited.add(type)) {
            // 循环引用：用类型名占位，避免无限递归
            snapshot.put("__cycle", type.getName());
            return snapshot;
        }
        try {
            var handles = HANDLE_CACHE.get(type);
            for (var entry : handles.entrySet()) {
                var value = entry.getValue().invoke(annotation);
                snapshot.put(entry.getKey(), normalizeForCacheKey(value, visited));
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            visited.remove(type);
        }
        return snapshot;
    }


    /**
     * 将注解属性值规范化为可用于 cache key 等价的形态（支持数组、Class、注解）。
     */
    private static Object normalizeForCacheKey(Object value, Set<Class<? extends Annotation>> visited) {
        if (value == null) {
            return null;
        }
        if (value.getClass().isArray()) {
            if (value instanceof Object[] arr) {
                return new ArrayKey(arr);
            }
            // 基本类型数组转成 Object[] 再包成 ArrayKey，保证按内容等价
            var len = Array.getLength(value);
            var boxed = new Object[len];
            for (int i = 0; i < len; i++) {
                boxed[i] = Array.get(value, i);
            }
            return new ArrayKey(boxed);
        }
        if (value instanceof Class<?> clazz) {
            return clazz.getName();
        }
        if (value instanceof Annotation ann) {
            return new AnnotationSnapshot(ann, visited);
        }
        return value;
    }

    /**
     * 用于 Map key 的数组包装，按内容 equals/hashCode
     */
    private record ArrayKey(Object[] array) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ArrayKey(Object[] array1))) return false;
            return Arrays.deepEquals(array, array1);
        }

        @Override
        public int hashCode() {
            return Arrays.deepHashCode(array);
        }
    }

    /**
     * 嵌套注解也做 snapshot，便于 key 等价
     */
    private static final class AnnotationSnapshot {
        private final Map<String, Object> attrs;

        AnnotationSnapshot(Annotation annotation, Set<Class<? extends Annotation>> visited) {
            this.attrs = new HashMap<>();
            var type = annotation.annotationType();
            if (!visited.add(type)) {
                attrs.put("__cycle", type.getName());
                return;
            }
            try {
                var handles = HANDLE_CACHE.get(type);
                for (var entry : handles.entrySet()) {
                    attrs.put(entry.getKey(), normalizeForCacheKey(entry.getValue().invoke(annotation), visited));
                }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            } finally {
                visited.remove(type);
            }
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AnnotationSnapshot that)) return false;
            return Objects.equals(attrs, that.attrs);
        }

        @Override
        public int hashCode() {
            return Objects.hash(attrs);
        }
    }
}
