package com.travis.infrastructure.common.validation.validator;

import com.travis.infrastructure.common.validation.annotation.Password;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** 密码校验器 长度 8~32 位 大写字母、小写字母、数字、特殊符号 至少包含其中 3 种 */
public class PasswordValidator implements ConstraintValidator<Password, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            // 未传入密码可通过校验，判空由其他注解处理
            return true;
        }
        if (value.length() < 8 || value.length() > 32) {
            return false;
        }

        var types = 0;

        // 小写字母
        if (value.matches(".*[a-z].*")) {
            types++;
        }

        // 大写字母
        if (value.matches(".*[A-Z].*")) {
            types++;
        }

        // 数字
        if (value.matches(".*\\d.*")) {
            types++;
        }

        // 特殊符号
        if (value.matches(".*[~!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            types++;
        }

        return types >= 3;
    }
}
