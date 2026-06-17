package com.travis.infrastructure.common.validation.validator;

import com.travis.infrastructure.common.validation.annotation.Username;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UsernameValidator implements ConstraintValidator<Username, String> {

    /** 用户名 英文字母开头的账号，支持字母、数字和下划线 */
    public static final String USERNAME = "^[a-zA-Z][a-zA-Z0-9_]*$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return value.matches(USERNAME);
    }
}
