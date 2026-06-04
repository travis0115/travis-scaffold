package com.travis.infrastructure.framework.logging.core.util;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONObject;
import com.travis.infrastructure.framework.logging.core.constant.LogKeys;
import com.travis.infrastructure.framework.logging.core.enums.AccessLogger;
import com.travis.infrastructure.framework.logging.core.enums.EventLogger;
import com.travis.infrastructure.framework.logging.core.enums.EventType;
import com.travis.infrastructure.framework.logging.core.enums.LogType;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArgument;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static net.logstash.logback.argument.StructuredArguments.kv;

/**
 * 事件日志工具类
 * <p>
 * 开发环境：通过 {@link DevLoggerUtil} 输出格式化盒式边框日志
 * 生产环境：通过 StructuredArguments 以 JSON 结构化日志输出
 * <p>
 * 输出示例：
 * <pre>
 * {
 *   "log_type": "EVENT",
 *   "event_type": "ORDER_CREATE",
 *   "order_id": "123456",
 *   "amount": "99.00"
 * }
 * </pre>
 */
@Slf4j
public final class EventLoggerUtil {

    private final static String logOutput = SpringUtil.getProperty("logging.output", AccessLogger.STDOUT.name());


    private EventLoggerUtil() {
    }

    /**
     * 记录事件日志
     *
     * @param eventType 事件类型
     * @param dataJson      事件附加数据
     */
    public static void log(EventType eventType, JSONObject dataJson) {
        var argumentsJson = new JSONObject();
        argumentsJson.set(LogKeys.LOG_TYPE, LogType.EVENT.name());
        argumentsJson.set(LogKeys.EVENT_TYPE, eventType.getCode());
        if (dataJson != null) {
            argumentsJson.putAll(dataJson);
        }

        if ("dev".equalsIgnoreCase(SpringUtil.getActiveProfile())) {
            DevLoggerUtil.print(log, "EVENT LOG ─ " + eventType.getDescription(), argumentsJson);
        }
        if ("prod".equalsIgnoreCase(SpringUtil.getActiveProfile())) {
            var argumentsList = new ArrayList<StructuredArgument>();
            argumentsJson.forEach(argument ->
                    argumentsList.add(kv(argument.getKey(), argument.getValue())));
            var logger = LoggerFactory.getLogger(EventLogger.from(logOutput).getLoggerName());
            logger.info(LogType.EVENT.name(), argumentsList.toArray());
        }
    }

    /**
     * 记录事件日志（无附加数据）
     *
     * @param eventType 事件类型
     */
    public static void log(EventType eventType) {
        log(eventType, null);
    }

}
