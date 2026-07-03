package com.travis.infrastructure.common.logging.annotation;

import com.travis.infrastructure.common.logging.enums.OperationBusinessType;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 记录后台关键业务操作。 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {

    /** 业务行为 */
    String action();

    /** 业务类型 */
    OperationBusinessType businessType() default OperationBusinessType.AUTO;

    /** 是否记录请求参数 */
    boolean recordRequest() default true;

    /** 是否记录返回结果 */
    boolean recordResponse() default true;
}
