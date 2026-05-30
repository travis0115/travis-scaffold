package com.travis.infrastructure.framework.web.core.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONObject;
import com.travis.infrastructure.common.web.constant.CustomHttpHeaders;
import com.travis.infrastructure.common.web.constant.MdcKeys;
import com.travis.infrastructure.common.web.enums.ClientType;
import com.travis.infrastructure.framework.desensitize.core.resolver.DesensitizeResolver;
import com.travis.infrastructure.framework.desensitize.core.rule.DesensitizeRule;
import com.travis.infrastructure.framework.desensitize.core.util.DesensitizeUtils;
import com.travis.infrastructure.framework.jackson.core.util.JsonUtils;
import com.travis.infrastructure.framework.logging.core.constant.LogKeys;
import com.travis.infrastructure.framework.logging.core.enums.LogType;
import com.travis.infrastructure.framework.logging.core.util.DevLoggerUtil;
import com.travis.infrastructure.framework.logging.core.enums.AccessLogger;
import com.travis.infrastructure.framework.web.core.utils.ServletUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArgument;
import org.jspecify.annotations.NonNull;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static net.logstash.logback.argument.StructuredArguments.kv;

/**
 * 访问日志过滤器
 */
@Slf4j
public class AccessLogFilter extends OncePerRequestFilter {

    private final HandlerExceptionResolver handlerExceptionResolver;
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final boolean enabledAccessLog = Boolean.parseBoolean(
            SpringUtil.getProperty("logging.access.enabled", "true"));
    private final String accessLogOutput = SpringUtil.getProperty("logging.output", AccessLogger.STDOUT.name());

    public AccessLogFilter(HandlerExceptionResolver handlerExceptionResolver,
                           RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) {
        var beginTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            handlerExceptionResolver.resolveException(request, response, null, e);
        } finally {
            outputAccessLog(request, response, beginTime);
        }
    }

    /**
     * 输出访问日志
     */
    private void outputAccessLog(HttpServletRequest request, HttpServletResponse response,
                                 long beginTime) {
        if (!enabledAccessLog) {
            return;
        }
        try {
            var handlerMethod = resolveHandlerMethod(request);

            var logType = LogType.ACCESS.name();
            var tenantId = request.getHeader(CustomHttpHeaders.TENANT_ID);
            var userId = request.getHeader(CustomHttpHeaders.USER_ID);
            var apiUrl = request.getRequestURI();
            var httpMethod = request.getMethod();
            var clientIp = ServletUtils.getClientIP(request);
            var userAgent = ServletUtils.getUserAgent();
            var clientType = ClientType.from(request.getHeader(CustomHttpHeaders.CLIENT_TYPE));
            var apiCost = (System.currentTimeMillis() - beginTime) + " ms";
            var requestParams = desensitizeRequestParams(request, handlerMethod);
            var requestBody = desensitizeRequestBody(request, handlerMethod);
            var apiResult = desensitizeResponseBody(response);

            var argumentsJson = new JSONObject();
            argumentsJson.set(MdcKeys.TENANT_ID, tenantId);
            argumentsJson.set(MdcKeys.USER_ID, userId);
            argumentsJson.set(LogKeys.LOG_TYPE, logType);
            argumentsJson.set(LogKeys.API_URL, apiUrl);
            argumentsJson.set(LogKeys.HTTP_METHOD, httpMethod);
            argumentsJson.set(LogKeys.CLIENT_IP, clientIp);
            argumentsJson.set(LogKeys.USER_AGENT, userAgent);
            argumentsJson.set(LogKeys.CLIENT_TYPE, clientType);
            argumentsJson.set(LogKeys.API_COST, apiCost);
            if (!requestParams.isEmpty()) {
                argumentsJson.set(LogKeys.REQUEST_PARAMS, requestParams);
            }
            if (StrUtil.isNotBlank(requestBody)) {
                argumentsJson.set(LogKeys.REQUEST_BODY, requestBody);
            }
            argumentsJson.set(LogKeys.API_RESULT, apiResult);

            if ("dev".equalsIgnoreCase(SpringUtil.getActiveProfile())) {
                var logger = LoggerFactory.getLogger(this.getClass());
                DevLoggerUtil.print(logger, "ACCESS LOG", argumentsJson);
            }
            if ("prod".equalsIgnoreCase(SpringUtil.getActiveProfile())) {
                var argumentsList = new ArrayList<StructuredArgument>();
                argumentsJson.forEach(argument ->
                        argumentsList.add(kv(argument.getKey(), argument.getValue())));
                var logger = LoggerFactory.getLogger(AccessLogger.from(accessLogOutput).getLoggerName());
                logger.info(LogType.ACCESS.name(), argumentsList.toArray());
            }
        } catch (Exception e) {
            log.error("Access log error", e);
        }
    }

    /**
     * 优先从 DispatcherServlet 已设置的请求属性中获取 HandlerMethod，
     * 避免二次路由匹配；若不存在则回退到 HandlerMapping 重新解析。
     */
    private HandlerMethod resolveHandlerMethod(HttpServletRequest request) {
        var handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
        if (handler instanceof HandlerMethod hm) {
            return hm;
        }
        try {
            var chain = requestMappingHandlerMapping.getHandler(request);
            if (chain != null && chain.getHandler() instanceof HandlerMethod hm) {
                return hm;
            }
        } catch (Exception ignored) {
        }
        return null;
    }


    /**
     * 找到 Controller 方法的 @RequestBody 参数类型，
     * 将原始 JSON 反序列化为该类型后重新序列化，Jackson 的 StringDesensitizeSerializer 自动处理脱敏注解。
     */
    private String desensitizeRequestBody(HttpServletRequest request, HandlerMethod handlerMethod) {
        var rawBody = ServletUtils.getCachedJsonBody(request);
        if (StrUtil.isBlank(rawBody) || "{}".equals(rawBody)) {
            return null;
        }

        if (handlerMethod == null) {
            return rawBody;
        }

        // 有 @RequestBody 参数：反序列化后重序列化，触发脱敏
        try {
            for (var param : handlerMethod.getMethodParameters()) {
                if (param.hasParameterAnnotation(RequestBody.class)) {
                    var genericType = param.getGenericParameterType();
                    var obj = JsonUtils.parseObject(rawBody, genericType);
                    if (obj != null) {
                        return JsonUtils.toJsonString(obj);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            log.warn("Access log body 脱敏失败", e);
        }

        // 没有 @RequestBody 或脱敏失败：原样记录 body（有数据就记，别吞掉）
        return rawBody;
    }


    /**
     * 对请求参数进行脱敏，支持两种场景：
     * 1. 散参数：@RequestParam String mobile — 检查方法参数上的脱敏注解
     * 2. DTO 参数：UserQueryDTO dto — 扫描 DTO 类字段上的脱敏注解，按字段名匹配参数
     */
    private Map<String, String> desensitizeRequestParams(HttpServletRequest request, HandlerMethod handlerMethod) {
        var rawParams = ServletUtils.getParamMap(request);
        if (rawParams.isEmpty() || handlerMethod == null) {
            return rawParams;
        }
        try {
            var result = new LinkedHashMap<>(rawParams);

            for (var param : handlerMethod.getMethodParameters()) {
                var paramType = param.getParameterType();

                if (BeanUtils.isSimpleValueType(paramType)) {
                    desensitizeSimpleRequestParam(param, result);
                } else if (isUserDefinedType(paramType)) {
                    desensitizeDtoRequestParams(paramType, result);
                }
            }

            return result;
        } catch (Exception e) {
            log.warn("Access log params 脱敏失败", e);
            return rawParams;
        }
    }

    /**
     * 散参数脱敏：检查方法参数上直接标注的脱敏注解
     */
    private void desensitizeSimpleRequestParam(MethodParameter param, Map<String, String> result) {
        var paramName = param.getParameterName();
        if (paramName == null || !result.containsKey(paramName)) {
            return;
        }
        for (var annotation : param.getParameterAnnotations()) {
            var rule = DesensitizeResolver.resolveRule(annotation);
            if (rule != null) {
                applyRule(result, paramName, rule);
                break;
            }
        }
    }

    /**
     * DTO 参数脱敏：遍历类字段（含父类），按字段名匹配请求参数，应用字段上的脱敏注解
     */
    private void desensitizeDtoRequestParams(Class<?> dtoType, Map<String, String> result) {
        var fieldRules = DesensitizeUtils.resolveFieldRules(dtoType);
        for (var entry : fieldRules.entrySet()) {
            applyRule(result, entry.getKey(), entry.getValue());
        }
    }

    /**
     * 返回体脱敏：尝试从 response 中获取 body 内容
     * Jackson 已脱敏,直接返回
     */
    private String desensitizeResponseBody(HttpServletResponse response) {
        if (!(response instanceof ContentCachingResponseWrapper)) {
            log.warn("response is not ContentCachingResponseWrapper");
            return null;
        }
        var content = ((ContentCachingResponseWrapper) response).getContentAsByteArray();
        if (content.length > 0) {
            var responseBody = new String(content, StandardCharsets.UTF_8);
            if (StrUtil.isNotBlank(responseBody)) {
                // 返回体已由 jackson 脱敏
                return responseBody;
            }
        }

        return null;

    }

    /**
     * 对参数值应用脱敏规则
     */
    private void applyRule(Map<String, String> params, String key, DesensitizeRule rule) {
        params.computeIfPresent(key, (_, v) -> rule.apply(v));
    }

    /**
     * 判断是否为用户自定义类型
     */
    private boolean isUserDefinedType(Class<?> type) {
        String name = type.getName();
        return !name.startsWith("java.")
                && !name.startsWith("javax.")
                && !name.startsWith("jakarta.")
                && !name.startsWith("org.springframework.");
    }


}