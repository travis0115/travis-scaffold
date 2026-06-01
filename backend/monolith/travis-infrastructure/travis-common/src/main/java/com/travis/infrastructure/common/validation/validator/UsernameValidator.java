package com.travis.infrastructure.common.validation.validator;

import com.travis.infrastructure.common.validation.annotation.Username;
import com.travis.infrastructure.common.validation.constant.RegexConstants;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UsernameValidator implements ConstraintValidator<Username, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return value.matches(RegexConstants.USERNAME);
    }
}