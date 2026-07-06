package com.travis.infrastructure.common.validation.annotation;

import com.travis.infrastructure.common.validation.validator.JsonValueValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({
    ElementType.METHOD,
    ElementType.FIELD,
    ElementType.ANNOTATION_TYPE,
    ElementType.CONSTRUCTOR,
    ElementType.PARAMETER,
    ElementType.TYPE_USE
})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = JsonValueValidator.class)
@Documented
public @interface JsonValue {

    String message() default "JSON格式错误";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    Type type() default Type.OBJECT;

    enum Type {
        /** JSON 对象。 */
        OBJECT,

        /** JSON 数组。 */
        ARRAY,

        /** 任意合法 JSON 值。 */
        ANY
    }
}
