package com.travis.infrastructure.framework.web.core.filter;

import cn.hutool.core.util.StrUtil;
import com.travis.infrastructure.common.web.constant.CustomHttpHeaders;
import com.travis.infrastructure.common.web.constant.MdcKeys;
import com.travis.infrastructure.framework.web.core.http.MutableHttpServletRequest;
import com.travis.infrastructure.framework.web.core.utils.IpUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MDCFilter
 * <p>
 * 职责：在请求入口将必要值写入MDC，并在出口清理
 * <p>
 * 放在网关 / 单体入口 都适用
 */
@Slf4j
public class MdcFilter extends OncePerRequestFilter {

    private final HandlerExceptionResolver handlerExceptionResolver;

    public MdcFilter(HandlerExceptionResolver handlerExceptionResolver) {
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) {
        var wrappedRequest = new MutableHttpServletRequest(request);
        try {
            //requestId
            var requestId = wrappedRequest.getHeader(CustomHttpHeaders.REQUEST_ID);
            if (StrUtil.isBlank(requestId)) {
                requestId = RequestIdGenerator.nextId();
                wrappedRequest.putHeader(CustomHttpHeaders.REQUEST_ID, requestId);
            }
            MDC.put(MdcKeys.REQUEST_ID, requestId);
            response.setHeader(CustomHttpHeaders.REQUEST_ID, requestId);

            // tenant_id
            var tenantId = request.getHeader(CustomHttpHeaders.TENANT_ID);
            if (tenantId != null) {
                MDC.put(MdcKeys.TENANT_ID, tenantId);
            }

            // client_ip
            MDC.put(MdcKeys.CLIENT_IP, IpUtils.getClientIp(request));

            filterChain.doFilter(wrappedRequest, response);
        } catch (Exception e) {
            handlerExceptionResolver.resolveException(wrappedRequest, response, null, e);
        } finally {
            MDC.clear();
        }
        // 禁止执行完毕后添加代码
    }

    /**
     * 是否允许在发生错误时过滤请求
     */
    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    /**
     * 是否允许在异步处理时过滤请求
     */
    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    /**
     * requestId 生成器，Base36 短 ID。
     * 布局（64 bit）：高 42 位时间（毫秒，截断防溢出），中 12 位同毫秒序号，低 20 位随机数。
     */
    private static final class RequestIdGenerator {

        private static final AtomicInteger SEQ = new AtomicInteger();
        private static volatile long lastTime = -1L;

        private static final long TIME_MASK = (1L << 42) - 1;

        public static String nextId() {
            long now = System.currentTimeMillis();
            int seq;
            if (now == lastTime) {
                seq = SEQ.incrementAndGet() & 0xFFF;
            } else {
                synchronized (RequestIdGenerator.class) {
                    if (now != lastTime) {
                        SEQ.set(0);
                        lastTime = now;
                    }
                    seq = SEQ.incrementAndGet() & 0xFFF;
                }
            }
            int rand = ThreadLocalRandom.current().nextInt(1 << 20);
            long time42 = now & TIME_MASK;
            long value = (time42 << 22) | ((long) seq << 20) | rand;
            return Long.toUnsignedString(value, 36);
        }
    }

}