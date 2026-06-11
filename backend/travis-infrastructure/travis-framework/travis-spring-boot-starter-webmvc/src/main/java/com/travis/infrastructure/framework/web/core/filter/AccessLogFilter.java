package com.travis.infrastructure.framework.web.core.filter;

import static net.logstash.logback.argument.StructuredArguments.kv;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONObject;
import com.travis.infrastructure.common.web.constant.CustomHttpHeader;
import com.travis.infrastructure.common.web.constant.MdcKey;
import com.travis.infrastructure.common.web.enums.ClientType;
import com.travis.infrastructure.framework.desensitize.core.resolver.DesensitizeResolver;
import com.travis.infrastructure.framework.desensitize.core.rule.DesensitizeRule;
import com.travis.infrastructure.framework.desensitize.core.util.DesensitizeUtil;
import com.travis.infrastructure.framework.jackson.core.JsonUtil;
import com.travis.infrastructure.framework.logging.core.constant.LogKeys;
import com.travis.infrastructure.framework.logging.core.enums.AccessLogger;
import com.travis.infrastructure.framework.logging.core.enums.LogType;
import com.travis.infrastructure.framework.logging.core.util.DevLoggerUtil;
import com.travis.infrastructure.framework.web.core.util.ServletUtil;
import com.travis.infrastructure.framework.web.core.util.UserAgentUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArgument;
import org.jspecify.annotations.NonNull;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.ContentCachingResponseWrapper;

/** 访问日志过滤器 */
@Slf4j
public class AccessLogFilter extends OncePerRequestFilter {

    private final HandlerExceptionResolver handlerExceptionResolver;
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final boolean enabledAccessLog =
            Boolean.parseBoolean(SpringUtil.getProperty("logging.access.enabled", "true"));
    private final String logOutput =
            SpringUtil.getProperty("logging.output", AccessLogger.STDOUT.name());
    private final String tokenName =
            SpringUtil.getProperty("sa-token.token-name", HttpHeaders.AUTHORIZATION);

    public AccessLogFilter(
            HandlerExceptionResolver handlerExceptionResolver,
            RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
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

    /** 输出访问日志 */
    private void outputAccessLog(
            HttpServletRequest request, HttpServletResponse response, long beginTime) {
        if (!enabledAccessLog || HttpMethod.OPTIONS.name().equalsIgnoreCase(request.getMethod())) {
            return;
        }
        // 仅对 API 请求输出访问日志，跳过静态资源等请求
        if (!request.getRequestURI().startsWith("/api/")) {
            return;
        }
        try {
            var handlerMethod = resolveHandlerMethod(request);

            var logType = LogType.ACCESS.name();

            var apiUrl = request.getRequestURI();
            var httpMethod = request.getMethod();
            var tenantId = MDC.get(MdcKey.TENANT_ID);
            var userId = MDC.get(MdcKey.USER_ID);
            var clientIp = MDC.get(MdcKey.CLIENT_IP);
            var clientType =
                    ClientType.from(request.getHeader(CustomHttpHeader.CLIENT_TYPE))
                            .getDisplayName();
            var userAgent = UserAgentUtil.getCurrentUserAgentInfo(request);
            var platfromType = userAgent.getOs();
            var browser = userAgent.getBrowser();
            var apiCost = System.currentTimeMillis() - beginTime;
            var requestParams = desensitizeRequestParams(request, handlerMethod);
            var requestBody = desensitizeRequestBody(request, handlerMethod);
            var apiResult = desensitizeResponseBody(response);

            var argumentsJson = new JSONObject();
            argumentsJson.set(LogKeys.LOG_TYPE, logType);
            argumentsJson.set(LogKeys.API_URL, apiUrl);
            argumentsJson.set(LogKeys.HTTP_METHOD, httpMethod);
            argumentsJson.set(MdcKey.TENANT_ID, tenantId);
            argumentsJson.set(MdcKey.USER_ID, userId);
            argumentsJson.set(LogKeys.CLIENT_IP, clientIp);
            argumentsJson.set(LogKeys.CLIENT_TYPE, clientType);
            argumentsJson.set(LogKeys.PLATFORM_TYPE, platfromType);
            argumentsJson.set(LogKeys.BROWSER, browser);
            argumentsJson.set(LogKeys.API_COST, apiCost);
            if (!requestParams.isEmpty()) {
                argumentsJson.set(LogKeys.REQUEST_PARAMS, requestParams);
            }
            if (StrUtil.isNotBlank(requestBody)) {
                argumentsJson.set(LogKeys.REQUEST_BODY, requestBody);
            }
            argumentsJson.set(LogKeys.API_RESULT, apiResult);

            if ("dev".equalsIgnoreCase(SpringUtil.getActiveProfile())) {
                DevLoggerUtil.print(log, "ACCESS LOG", argumentsJson);
            }
            if ("prod".equalsIgnoreCase(SpringUtil.getActiveProfile())) {
                var argumentsList = new ArrayList<StructuredArgument>();
                argumentsJson.forEach(
                        argument -> argumentsList.add(kv(argument.getKey(), argument.getValue())));
                var logger = LoggerFactory.getLogger(AccessLogger.from(logOutput).getLoggerName());
                logger.info(LogType.ACCESS.name(), argumentsList.toArray());
            }

        } catch (Exception e) {
            log.error("Access log error", e);
        }
    }

    /** 优先从 DispatcherServlet 已设置的请求属性中获取 HandlerMethod， 避免二次路由匹配；若不存在则回退到 HandlerMapping 重新解析。 */
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
     * 找到 Controller 方法的 @RequestBody 参数类型， 将原始 JSON 反序列化为该类型后重新序列化，Jackson 的
     * StringDesensitizeSerializer 自动处理脱敏注解。
     */
    private String desensitizeRequestBody(HttpServletRequest request, HandlerMethod handlerMethod) {
        var rawBody = ServletUtil.getCachedJsonBody(request);
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
                    var obj = JsonUtil.parseObject(rawBody, genericType);
                    if (obj != null) {
                        return JsonUtil.toJsonString(obj);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            // 脱敏失败：记录原始请求体
            log.warn("Access log body 脱敏失败, requestUrl={}", request.getRequestURI(), e);
        }

        // 没有 @RequestBody 或脱敏失败：原样记录 body（有数据就记，别吞掉）
        return rawBody;
    }

    /**
     * 对请求参数进行脱敏，支持两种场景： 1. 散参数：@RequestParam String mobile — 检查方法参数上的脱敏注解 2. DTO 参数：UserQueryDTO
     * dto — 扫描 DTO 类字段上的脱敏注解，按字段名匹配参数
     */
    private Map<String, String> desensitizeRequestParams(
            HttpServletRequest request, HandlerMethod handlerMethod) {
        var rawParams = ServletUtil.getParamMap(request);
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
            log.warn("Access log params 脱敏失败, requestUrl={}", request.getRequestURI(), e);
            return rawParams;
        }
    }

    /** 散参数脱敏：检查方法参数上直接标注的脱敏注解 */
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

    /** DTO 参数脱敏：遍历类字段（含父类），按字段名匹配请求参数，应用字段上的脱敏注解 */
    private void desensitizeDtoRequestParams(Class<?> dtoType, Map<String, String> result) {
        var fieldRules = DesensitizeUtil.resolveFieldRules(dtoType);
        for (var entry : fieldRules.entrySet()) {
            applyRule(result, entry.getKey(), entry.getValue());
        }
    }

    /** 返回体脱敏：尝试从 response 中获取 body 内容 Jackson 已脱敏,直接返回 */
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

    /** 对参数值应用脱敏规则 */
    private void applyRule(Map<String, String> params, String key, DesensitizeRule rule) {
        params.computeIfPresent(key, (_, v) -> rule.apply(v));
    }

    /** 判断是否为用户自定义类型 */
    private boolean isUserDefinedType(Class<?> type) {
        String name = type.getName();
        return !name.startsWith("java.")
                && !name.startsWith("javax.")
                && !name.startsWith("jakarta.")
                && !name.startsWith("org.springframework.");
    }
}
