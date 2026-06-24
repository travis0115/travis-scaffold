package com.travis.monolith.system.config.api;

/** 系统配置对外查询 API。 */
public interface SysConfigApi {

    /** 根据配置键获取配置值 */
    String getValue(String configKey);
}
