package com.travis.infrastructure.common.web.constant;

import com.travis.infrastructure.common.web.enums.ClientType;

/**
 * 自定义 HTTP 请求 / 响应头常量定义
 * <p>
 * 命名与 RFC / 业界约定保持一致
 */

public final class CustomHttpHeaders {

    /* ==================== Trace / Request ==================== */
    /**
     * 请求唯一标识
     */
    public static final String REQUEST_ID = "X-Request-Id";

    /**
     * 分布式追踪 TraceId
     */
    public static final String TRACE_ID = "X-Trace-Id";

    /* ==================== Auth ==================== */
    //    例如：public static final String AUTHORIZATION = "Authorization";

    /* ==================== Client ==================== */

    /**
     * 客户端类型 {@link ClientType}
     */
    public static final String CLIENT_TYPE = "Client-Type";


    /* ==================== User ==================== */
    /**
     * 租户 ID
     */
    public static final String TENANT_ID = "X-Tenant-Id";

    /**
     * 用户 ID
     */
    public static final String USER_ID = "X-User-Id";


    private CustomHttpHeaders() {
    }
}
