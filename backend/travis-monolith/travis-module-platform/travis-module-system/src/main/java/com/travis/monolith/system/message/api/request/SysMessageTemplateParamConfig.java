package com.travis.monolith.system.message.api.request;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.monolith.system.message.api.enums.SysMessageTemplateParamType;
import lombok.Data;

/** 消息模板参数配置。 */
@Data
public class SysMessageTemplateParamConfig {
    private String label;

    @EnumValue(value = SysMessageTemplateParamType.class, message = "参数类型错误")
    private String type;

    private Boolean required;
    private String description;
}
