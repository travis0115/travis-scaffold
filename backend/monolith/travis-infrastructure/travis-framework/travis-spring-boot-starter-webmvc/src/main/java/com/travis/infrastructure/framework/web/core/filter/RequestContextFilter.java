package com.travis.infrastructure.framework.web.core.filter;

import com.travis.infrastructure.framework.web.core.utils.ServletUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

/**
 * 请求上下文过滤器
 */
@Slf4j
public class RequestContextFilter extends OncePerRequestFilter {

    private final HandlerExceptionResolver handlerExceptionResolver;

    public RequestContextFilter(HandlerExceptionResolver handlerExceptionResolver) {
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) {

        //包装ContentCachingRequestWrapper支持重复读取body
        var requestWrapper = ServletUtils.isJsonRequest(request) ?
                new ContentCachingRequestWrapper(request, 0) : request;
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