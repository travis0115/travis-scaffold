package com.travis.infrastructure.common.validation.validator;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.travis.infrastructure.common.validation.annotation.JsonValue;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class JsonValueValidator implements ConstraintValidator<JsonValue, String> {

    private JsonValue.Type type;

    @Override
    public void initialize(JsonValue annotation) {
        this.type = annotation.type();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StrUtil.isBlank(value)) {
            return true;
        }
        return switch (type) {
            case OBJECT -> JSONUtil.isTypeJSONObject(value);
            case ARRAY -> JSONUtil.isTypeJSONArray(value);
            case ANY -> JSONUtil.isTypeJSON(value);
        };
    }
}
