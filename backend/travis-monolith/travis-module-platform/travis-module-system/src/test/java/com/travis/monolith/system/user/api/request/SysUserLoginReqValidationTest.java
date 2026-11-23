package com.travis.monolith.system.user.api.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SysUserLoginReqValidationTest {

    @ParameterizedTest
    @ValueSource(strings = {"Admin718.", "admin718"})
    void shouldAllowDefaultAndLegacyPasswords(String password) {
        var request = new SysUserLoginReq();
        request.setUsername("admin718");
        request.setPassword(password);

        try (var factory = Validation.buildDefaultValidatorFactory()) {
            assertThat(factory.getValidator().validate(request)).isEmpty();
        }
    }
}
