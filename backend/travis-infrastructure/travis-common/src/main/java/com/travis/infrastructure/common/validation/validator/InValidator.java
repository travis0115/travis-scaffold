package com.travis.infrastructure.common.validation.validator;

import com.travis.infrastructure.common.validation.annotation.In;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class InValidator implements ConstraintValidator<In, Integer> {

    private int[] values;

    @Override
    public void initialize(In annotation) {
        values = annotation.value();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return Arrays.stream(values).anyMatch(item -> item == value);
    }
}
