package com.travis.monolith.system.user.api.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

class SysUserLoginReqValidationTest {

    @Test
    void shouldAllowExistingPasswordWithoutApplyingCreationComplexityRules() {
        var request = new SysUserLoginReq();
        request.setUsername("admin718");
        request.setPassword("admin718");

        try (var factory = Validation.buildDefaultValidatorFactory()) {
            assertThat(factory.getValidator().validate(request)).isEmpty();
        }
    }
}
