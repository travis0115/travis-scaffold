package com.travis.infrastructure.framework.web.core.util;

import com.travis.infrastructure.common.monitor.error.ErrorReporter;
import com.travis.infrastructure.framework.desensitize.core.Desensitizer;
import com.travis.infrastructure.framework.jackson.core.JsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

/** 为系统错误日志采集严格脱敏、限长的 HTTP 请求快照。 */
@Slf4j
@RequiredArgsConstructor
public class ErrorRequestSnapshotter {

    private final Desensitizer desensitizer;

    public Snapshot snapshot(HttpServletRequest request) {
        var handlerMethod = resolveHandlerMethod(request);
        if (handlerMethod == null) {
            return new Snapshot(null, null);
        }
        return new Snapshot(
                handlerMethod.getBeanType().getName() + "#" + handlerMethod.getMethod().getName(),
                requestParams(request, handlerMethod));
    }

    private HandlerMethod resolveHandlerMethod(HttpServletRequest request) {
        var handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
        return handler instanceof HandlerMethod handlerMethod ? handlerMethod : null;
    }

    private String requestParams(HttpServletRequest request, HandlerMethod handlerMethod) {
        var rawParams = new LinkedHashMap<>(ServletUtil.getParamMap(request));
        var pathVariables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathVariables instanceof Map<?, ?> variables) {
            variables.forEach(
                    (key, value) -> rawParams.put(String.valueOf(key), String.valueOf(value)));
        }
        if (rawParams.isEmpty()) {
            return null;
        }
        try {
            var result =
                    desensitizer.desensitizeParameters(
                            rawParams, handlerMethod.getMethodParameters());
            if (result.isEmpty()) {
                return null;
            }
            return ErrorReporter.truncate(
                    JsonUtil.toJsonString(result), ErrorReporter.MAX_CONTEXT_LENGTH);
        } catch (Exception exception) {
            log.warn("错误日志请求参数序列化失败，已放弃记录，requestUrl={}", request.getRequestURI());
            return null;
        }
    }

    public record Snapshot(String controllerMethod, String requestParams) {}
}
