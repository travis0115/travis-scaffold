package com.travis.infrastructure.common.web.constant;

import com.travis.infrastructure.common.web.enums.ClientType;

/**
 * 自定义 HTTP 请求 / 响应头常量定义
 *
 * <p>命名与 RFC / 业界约定保持一致
 */
public final class HttpHeader {

    /* ==================== IP ==================== */
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String X_REAL_IP = "X-Real-IP";
    public static final String PROXY_CLIENT_IP = "Proxy-Client-IP";
    public static final String WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";
    public static final String HTTP_CLIENT_IP = "HTTP_CLIENT_IP";
    public static final String HTTP_X_FORWARDED_FOR = "HTTP_X_FORWARDED_FOR";

    /* ==================== Trace / Request ==================== */
    /** 请求唯一标识 */
    public static final String REQUEST_ID = "X-Request-Id";

    /** 分布式追踪 traceparent */
    public static final String TRACEPARENT = "traceparent";


    /* ==================== Client ==================== */

    /** 客户端类型 {@link ClientType} */
    public static final String CLIENT_TYPE = "Client-Type";

    /* ==================== User ==================== */
    /** 租户 ID */
    public static final String TENANT_ID = "Tenant-Id";

    /** 用户 ID */
    public static final String USER_ID = "User-Id";

    private HttpHeader() {}
}
