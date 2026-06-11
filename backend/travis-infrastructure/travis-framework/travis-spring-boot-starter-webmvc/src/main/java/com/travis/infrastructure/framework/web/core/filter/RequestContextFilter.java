package com.travis.infrastructure.framework.web.core.filter;

import com.travis.infrastructure.framework.web.core.util.ServletUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

/** 请求上下文过滤器 */
@Slf4j
public class RequestContextFilter extends OncePerRequestFilter {

    private final HandlerExceptionResolver handlerExceptionResolver;
    private final int requestCacheLimit;

    public RequestContextFilter(
            HandlerExceptionResolver handlerExceptionResolver, int requestCacheLimit) {
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.requestCacheLimit = requestCacheLimit;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) {

        // multipart/form-data 请求不使用 ContentCachingRequestWrapper
        // 因为 Spring 的 MultipartResolver 需要直接读取原始请求流
        boolean isJsonRequest = ServletUtil.isJsonRequest(request);
        var requestWrapper =
                isJsonRequest
                        ? new ContentCachingRequestWrapper(request, requestCacheLimit)
                        : request;
        var responseWrapper = new ContentCachingResponseWrapper(response);
        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } catch (Exception e) {
            handlerExceptionResolver.resolveException(requestWrapper, responseWrapper, null, e);
        } finally {
            try {
                responseWrapper.copyBodyToResponse();
            } catch (IOException e) {
                log.error("copy body to response error", e);
            }
        }
    }
}
