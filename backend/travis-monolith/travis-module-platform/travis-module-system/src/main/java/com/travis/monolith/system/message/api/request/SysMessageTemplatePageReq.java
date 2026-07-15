package com.travis.monolith.system.message.api.request;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.web.model.PageRequest;
import com.travis.monolith.system.common.api.enums.Status;
import com.travis.monolith.system.message.api.enums.SysMessageChannel;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 消息模板分页查询请求参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysMessageTemplatePageReq extends PageRequest {
    /** 模板编码，支持模糊匹配。 */
    @Size(max = 64, message = "模板编码长度不能超过64个字符")
    private String templateCode;

    /** 模板名称，支持模糊匹配。 */
    @Size(max = 100, message = "模板名称长度不能超过100个字符")
    private String templateName;

    /** 外部平台模板 ID。 */
    @Size(max = 128, message = "平台模板ID长度不能超过128个字符")
    private String platformTemplateId;

    /** 推送通道。 */
    @EnumValue(value = SysMessageChannel.class, message = "推送通道错误")
    private String channel;

    /** 模板状态。 */
    @EnumValue(value = Status.class, message = "状态值错误")
    private Integer status;
}
