package com.travis.infrastructure.common.monitor.error;

/** 系统未预期异常来源。 */
public enum ErrorSource {
    WEB,
    QUARTZ,
    ROCKETMQ,
    WEBSOCKET,
    SCHEDULING,
    ASYNC
}
