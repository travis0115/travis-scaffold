package com.travis.monolith.system.message.api.request;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.web.model.PageRequest;
import com.travis.monolith.system.common.api.enums.Status;
import com.travis.monolith.system.message.api.enums.SysMessageChannel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 消息模板分页查询请求参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysMessageTemplatePageReq extends PageRequest {
    private String templateCode;
    private String templateName;

    @EnumValue(value = SysMessageChannel.class, message = "推送通道错误")
    private String channel;

    @EnumValue(value = Status.class, message = "状态值错误")
    private Integer status;
}
