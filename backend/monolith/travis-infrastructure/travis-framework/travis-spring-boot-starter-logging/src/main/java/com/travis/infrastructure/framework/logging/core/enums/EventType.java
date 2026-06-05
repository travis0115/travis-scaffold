package com.travis.infrastructure.framework.logging.core.enums;

/**
 * 日志事件类型
 *
 * @author Travis
 */
public interface EventType {

    /** 事件代码 */
    String getCode();

    /** 事件说明 */
    String getDescription();
}
