package com.travis.infrastructure.framework.desensitize.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.travis.infrastructure.framework.desensitize.core.annotation.RegexDesensitize;
import com.travis.infrastructure.framework.desensitize.core.annotation.SliderDesensitize;
import com.travis.infrastructure.framework.desensitize.core.annotation.slider.MobileDesensitize;
import com.travis.infrastructure.framework.desensitize.core.jackson.modules.DesensitizeJacksonModule;
import com.travis.infrastructure.framework.desensitize.core.rule.SliderDesensitizeRule;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.LinkedHashMap;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import tools.jackson.databind.json.JsonMapper;

class DesensitizerTest {

    private Desensitizer desensitizer;

    @BeforeEach
    void setUp() {
        var objectMapper = JsonMapper.builder().addModule(new DesensitizeJacksonModule()).build();
        desensitizer = new Desensitizer(objectMapper);
    }

    @Test
    void shouldDesensitizeStringByRule() {
        var rule = new SliderDesensitizeRule(3, 4, '*');

        assertThat(desensitizer.desensitize("13800138000", rule)).isEqualTo("138****8000");
    }

    @Test
    void shouldDesensitizeObjectByAnnotationsAndFieldNames() {
        var result =
                desensitizer.desensitizeObject(
                        new JsonRequest(
                                "13800138000",
                                "plain-password",
                                new NestedRequest("plain-token", "value")));

        assertThat(result)
                .contains("\"mobile\":\"138****8000\"")
                .contains("\"password\":\"******\"")
                .contains("\"token\":\"******\"")
                .contains("\"visible\":\"value\"");
    }

    @Test
    void shouldApplyAnnotationRulesAndSensitiveFieldFallback() throws Exception {
        var parameters = new LinkedHashMap<String, String>();
        parameters.put("directMobile", "13800138000");
        parameters.put("mobile", "13800138000");
        parameters.put("password", "plain-password");
        parameters.put("api_key", "plain-api-key");
        parameters.put("visible", "value");

        var result =
                desensitizer.desensitizeParameters(
                        parameters,
                        methodParameters("handle", String.class, ParameterRequest.class),
                        Set.of("api-key"));

        assertThat(result)
                .containsEntry("directMobile", "138****8000")
                .containsEntry("mobile", "138****8000")
                .containsEntry("password", "******")
                .containsEntry("api_key", "******")
                .containsEntry("visible", "value");
    }

    @Test
    void shouldReturnEmptyParametersWhenDesensitizationFails() throws Exception {
        var result =
                desensitizer.desensitizeParameters(
                        java.util.Map.of("value", "secret"),
                        methodParameters("invalid", InvalidRequest.class));

        assertThat(result).isEmpty();
    }

    @Test
    void shouldDesensitizeJsonByAnnotationsAndFieldNames() {
        var json =
                """
                {
                  "mobile": "13800138000",
                  "password": "plain-password",
                  "nested": {"token": "plain-token", "visible": "value"}
                }
                """;

        var result = desensitizer.desensitizeJson(json, JsonRequest.class);

        assertThat(result)
                .contains("\"mobile\":\"138****8000\"")
                .contains("\"password\":\"******\"")
                .contains("\"token\":\"******\"")
                .contains("\"visible\":\"value\"");
    }

    @Test
    void shouldMaskAdditionalJsonFieldsWithoutTargetType() {
        var result =
                desensitizer.desensitizeJson(
                        "{\"nested\":{\"api_key\":\"plain-api-key\"}}", null, Set.of("api-key"));

        assertThat(result).isEqualTo("{\"nested\":{\"api_key\":\"******\"}}");
    }

    @Test
    void shouldReturnNullWhenJsonDesensitizationFails() {
        assertThat(desensitizer.desensitizeJson("{invalid", null)).isNull();
    }

    private MethodParameter[] methodParameters(String methodName, Class<?>... parameterTypes)
            throws Exception {
        var method = TestController.class.getDeclaredMethod(methodName, parameterTypes);
        var parameters = new MethodParameter[parameterTypes.length];
        for (int index = 0; index < parameterTypes.length; index++) {
            parameters[index] = new MethodParameter(method, index);
        }
        return parameters;
    }

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @SliderDesensitize(prefix = 3, suffix = 4)
    private @interface ParameterMobile {}

    private static class TestController {
        void handle(@ParameterMobile String directMobile, ParameterRequest request) {}

        void invalid(InvalidRequest request) {}
    }

    private static class ParameterRequest {
        @MobileDesensitize private String mobile;
    }

    private static class InvalidRequest {
        @RegexDesensitize(regex = "[")
        private String value;
    }

    private record JsonRequest(
            @MobileDesensitize String mobile, String password, NestedRequest nested) {}

    private record NestedRequest(String token, String visible) {}
}
