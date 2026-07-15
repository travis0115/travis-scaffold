package com.travis.monolith.system.message.api.request;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.monolith.system.message.api.enums.SysMessageTemplateParamType;
import lombok.Data;

/** 消息模板参数配置。 */
@Data
public class SysMessageTemplateParamConfigReq {
    /** 参数展示名称。 */
    private String label;

    /** 参数数据类型。 */
    @EnumValue(value = SysMessageTemplateParamType.class, message = "参数类型错误")
    private String type;

    /** 是否必填。 */
    private Boolean required;

    /** 参数用途说明。 */
    private String description;
}
