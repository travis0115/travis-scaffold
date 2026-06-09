package com.travis.infrastructure.common.monitor;

import com.travis.infrastructure.common.web.constant.MdcKey;
import org.slf4j.MDC;

/**
 * 链路追踪工具类
 *
 * <p>
 *
 * @author travis
 */
public class TracerUtil {

    /** 私有化构造方法 */
    private TracerUtil() {}

    /** 获得TraceId，直接返回 MDC 的 TraceId。 */
    public static String getTraceId() {
        return MDC.get(MdcKey.TRACE_ID);
    }

    /** 获得RequestId，直接返回 MDC 的 RequestId。 */
    public static String getRequestId() {
        return MDC.get(MdcKey.REQUEST_ID);
    }
}
