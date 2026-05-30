package com.travis.infrastructure.framework.web.core.exception.advice;

import com.travis.infrastructure.framework.web.core.model.ApiResponse;
import com.travis.infrastructure.framework.web.core.service.I18nService;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
@ConditionalOnProperty(
        prefix = "travis.application.web.i18n",
        name = "enabled",
        havingValue = "true"
)
public class I18nResponseBodyAdvice implements ResponseBodyAdvice<Object> {
    private final I18nService i18nService;

    public I18nResponseBodyAdvice(I18nService i18nService) {
        this.i18nService = i18nService;
    }

    @Override
    public boolean supports(@NonNull MethodParameter returnType,
                            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  @NonNull MethodParameter returnType,
                                  @NonNull MediaType selectedContentType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @NonNull ServerHttpRequest request,
                                  @NonNull ServerHttpResponse response) {

        if (body instanceof ApiResponse<?> result) {
            if (result.getCode() != null) {
                var translatedMsg = i18nService.getMessage(result.getCode(), result.getArgs(), result.getMsg());
                result.setMsg(translatedMsg);
            }
        }

        return body;
    }
}
