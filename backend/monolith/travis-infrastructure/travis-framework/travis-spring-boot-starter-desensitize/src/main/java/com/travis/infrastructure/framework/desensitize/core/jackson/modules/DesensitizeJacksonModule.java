package com.travis.infrastructure.framework.desensitize.core.jackson.modules;

import com.travis.infrastructure.framework.desensitize.core.annotation.DesensitizeBy;
import com.travis.infrastructure.framework.desensitize.core.jackson.serializer.StringDesensitizeSerializer;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.Annotated;
import tools.jackson.databind.introspect.NopAnnotationIntrospector;
import tools.jackson.databind.module.SimpleModule;

import java.lang.annotation.Annotation;

/**
 * Jackson Module：自动为带有 @DesensitizeBy 元注解的 String 字段绑定 StringDesensitizeSerializer
 */
public class DesensitizeJacksonModule extends SimpleModule {

    public DesensitizeJacksonModule() {
        super("DesensitizeModule");
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        context.insertAnnotationIntrospector(new DesensitizeAnnotationIntrospector());
    }

    /**
     * 自定义 AnnotationIntrospector：
     * 扫描字段/方法注解链，如果存在被 @DesensitizeBy 标记的注解，
     * 就告诉 Jackson 使用 StringDesensitizeSerializer。
     */
    static class DesensitizeAnnotationIntrospector extends NopAnnotationIntrospector {

        @Override
        public Object findSerializer(MapperConfig<?> config, Annotated am) {
            if (hasDesensitizeAnnotation(am)) {
                return StringDesensitizeSerializer.class;
            }
            return null;
        }


        private boolean hasDesensitizeAnnotation(Annotated annotated) {
            for (Annotation ann : annotated.annotations().toList()) {
                if (isDesensitize(ann)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 判断注解本身或其元注解链上是否存在 @DesensitizeBy
         */
        private boolean isDesensitize(Annotation annotation) {
            var type = annotation.annotationType();
            if (type.isAnnotationPresent(DesensitizeBy.class)) {
                return true;
            }
            // 递归一层：业务注解 → 策略注解 → @DesensitizeBy
            for (Annotation meta : type.getAnnotations()) {
                if (meta.annotationType().isAnnotationPresent(DesensitizeBy.class)) {
                    return true;
                }
            }
            return false;
        }
    }
}