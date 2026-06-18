package com.travis.monolith.system.config.api;

/** 系统配置对外查询 API。 */
public interface SysConfigApi {

    /** 根据配置键获取配置值，不存在则返回 null。 */
    String getValue(String configKey);
}
