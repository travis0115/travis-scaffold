package com.travis.infrastructure.common.web.constant;

/**
 * MDC key
 */
public final class MdcKeys {
    /**
     * 请求链路ID
     */
    public static final String TRACE_ID = "traceId";

    /**
     * 节点ID
     */
    public static final String SPAN_ID = "spanId";


    /**
     * 请求ID
     */
    public static final String REQUEST_ID = "request_id";


    /**
     * 服务名称
     */
    public static final String SERVICE = "service";

    /**
     * 服务实例
     */
    public static final String SERVICE_INSTANCE = "service_instance";

    /**
     * 租户ID
     */
    public static final String TENANT_ID = "tenant_id";

    /**
     * 用户ID
     */
    public static final String USER_ID = "user_id";

    private MdcKeys() {}

}
