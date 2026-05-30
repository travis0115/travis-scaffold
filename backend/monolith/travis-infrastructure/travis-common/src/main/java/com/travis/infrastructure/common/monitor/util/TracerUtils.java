package com.travis.infrastructure.common.monitor.util;

import com.travis.infrastructure.common.web.constant.MdcKeys;
import org.slf4j.MDC;

/**
 * 链路追踪工具类
 * <p>
 *
 * @author travis
 */
public class TracerUtils {

    /**
     * 私有化构造方法
     */
    private TracerUtils() {
    }

    /**
     * 获得TraceId，直接返回 MDC 的 TraceId。
     *
     */
    public static String getTraceId() {
        return MDC.get(MdcKeys.TRACE_ID);
    }

    /**
     * 获得RequestId，直接返回 MDC 的 RequestId。
     *
     */
    public static String getRequestId() {
        return MDC.get(MdcKeys.REQUEST_ID);
    }

}
