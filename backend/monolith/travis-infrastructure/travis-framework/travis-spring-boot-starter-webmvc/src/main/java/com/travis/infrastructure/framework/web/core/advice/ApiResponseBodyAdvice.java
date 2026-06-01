package com.travis.infrastructure.framework.web.core.advice;

import cn.hutool.core.util.StrUtil;
import com.travis.infrastructure.framework.web.core.model.ApiResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.text.MessageFormat;

/**
 * I18n 未启用时，格式化响应消息
 */
@RestControllerAdvice
public class ApiResponseBodyAdvice implements ResponseBodyAdvice<Object> {

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
            if (result.getArgs() != null && result.getArgs().length > 0 && StrUtil.isNotBlank(result.getMsg())) {
                var msg = MessageFormat.format(result.getMsg(), result.getArgs());
                result.setMsg(msg);
            }
        }

        return body;
    }
}
