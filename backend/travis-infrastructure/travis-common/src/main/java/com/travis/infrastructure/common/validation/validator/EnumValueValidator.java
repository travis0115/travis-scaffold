package com.travis.infrastructure.common.validation.validator;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class EnumValueValidator implements ConstraintValidator<EnumValue, Object> {

    private Set<Object> values;

    @Override
    public void initialize(EnumValue annotation) {
        Class<? extends Enum<?>> enumClass = annotation.value();
        try {
            Method getValue = enumClass.getMethod("getValue");
            values =
                    Arrays.stream(enumClass.getEnumConstants())
                            .map(item -> invokeGetValue(getValue, item))
                            .collect(Collectors.toSet());
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(enumClass.getName() + " 必须提供 getValue() 方法", e);
        }
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return values.stream().anyMatch(item -> Objects.equals(item, value));
    }

    private Object invokeGetValue(Method getValue, Enum<?> item) {
        try {
            return getValue.invoke(item);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException(item.getClass().getName() + " 的 getValue() 调用失败", e);
        }
    }
}
