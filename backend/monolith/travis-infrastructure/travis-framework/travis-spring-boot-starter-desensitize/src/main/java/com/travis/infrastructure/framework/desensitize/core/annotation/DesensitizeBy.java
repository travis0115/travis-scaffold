package com.travis.infrastructure.framework.desensitize.core.annotation;

import java.lang.annotation.*;

/**
 * 顶级脱敏元注解。标记在策略注解上，表示"这是一个脱敏注解"。
 * 纯元数据标记，不绑定任何序列化框架。
 */
@Documented
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DesensitizeBy {
}