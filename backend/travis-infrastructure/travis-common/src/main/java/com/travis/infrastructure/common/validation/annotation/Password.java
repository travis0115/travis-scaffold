package com.travis.infrastructure.common.validation.annotation;

import com.travis.infrastructure.common.validation.validator.PasswordValidator;
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
@Constraint(validatedBy = PasswordValidator.class)
@Documented
public @interface Password {

    String message() default "密码需为8-32位，并包含大写字母、小写字母、数字、特殊符号中的至少3种";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
