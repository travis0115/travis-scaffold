package com.travis.infrastructure.framework.logging.core.logback.provider;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import net.logstash.logback.composite.loggingevent.MdcJsonProvider;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JsonGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

/**
 * 支持脱敏的 MDC JSON Provider
 * 通过 LoggerContext 共享规则，避免 Classloader 隔离问题
 * <p>
 * 注入规则：
 * 1. 通过静态方法 registerRule 注册规则，规则会存储在 LoggerContext 中，确保与 Logback provider 实例共享同一份数据。
 * 2. 在 provider 实例中，通过 getContextRules 方法从 LoggerContext 中获取规则表，从而实现脱敏功能。
 * 例：DesensitizeMdcJsonProvider.registerRule(MdcKeys.TRACE_ID,new SliderDesensitizeRule(2, 2, '*')::apply);
 *
 * @author travis
 */
public class DesensitizeMdcJsonProvider extends MdcJsonProvider {

    private static final String RULES_KEY = DesensitizeMdcJsonProvider.class.getName() + ".rules";

    private final List<String> includeMdcKeyNames = new ArrayList<>();

    /**
     * 注册某个 MDC key 的脱敏规则。
     * 通过 LoggerContext 存储，确保与 Logback provider 实例共享同一份数据。
     */
    public static void registerRule(String mdcKey, UnaryOperator<String> rule) {
        getRules().put(mdcKey, rule);
    }


    @Override
    public void writeTo(JsonGenerator generator, ILoggingEvent event) {
        if (includeMdcKeyNames.isEmpty()) {
            super.writeTo(generator, event);
            return;
        }

        var mdcMap = event.getMDCPropertyMap();
        if (mdcMap == null || mdcMap.isEmpty()) {
            return;
        }

        var rules = getContextRules();

        for (var key : includeMdcKeyNames) {
            var value = mdcMap.get(key);
            if (value == null) {
                continue;
            }
            var rule = rules != null ? rules.get(key) : null;
            generator.writeStringProperty(key, rule != null ? rule.apply(value) : value);
        }
    }

    /**
     * 从 LoggerContext 获取规则表（provider 实例侧调用，通过 Logback 注入的 context）
     */
    private Map<String, UnaryOperator<String>> getContextRules() {
        var ctx = getContext();
        if (ctx == null) {
            return null;
        }
        return (Map<String, UnaryOperator<String>>) ctx.getObject(RULES_KEY);
    }

    /**
     * 从 LoggerFactory 获取 LoggerContext 并拿到/创建规则表（静态方法侧调用）
     */
    private static Map<String, UnaryOperator<String>> getRules() {
        var loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        var rules = (Map<String, UnaryOperator<String>>) loggerContext.getObject(RULES_KEY);
        if (rules == null) {
            rules = new ConcurrentHashMap<>();
            loggerContext.putObject(RULES_KEY, rules);
        }
        return rules;
    }


    public void addIncludeMdcKeyName(String mdcKeyName) {
        includeMdcKeyNames.add(mdcKeyName);
    }

}