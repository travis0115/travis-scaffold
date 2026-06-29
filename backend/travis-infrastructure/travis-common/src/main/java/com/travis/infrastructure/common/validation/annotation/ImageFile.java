package com.travis.infrastructure.common.validation.annotation;

import com.travis.infrastructure.common.validation.validator.ImageFileValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ImageFileValidator.class)
@Documented
public @interface ImageFile {

    String message() default "只能上传图片文件";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
