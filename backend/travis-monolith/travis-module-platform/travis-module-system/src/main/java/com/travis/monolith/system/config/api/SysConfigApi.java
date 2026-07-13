package com.travis.monolith.system.config.api;

import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;

/** 系统配置对外查询 API。 */
@Validated
public interface SysConfigApi {

    /** 根据配置键获取配置值 */
    String getValue(@NotBlank(message = "配置键不能为空") String configKey);
}
