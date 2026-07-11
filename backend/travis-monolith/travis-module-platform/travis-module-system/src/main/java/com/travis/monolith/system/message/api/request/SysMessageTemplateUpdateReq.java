package com.travis.monolith.system.message.api.request;

import cn.hutool.core.util.StrUtil;
import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.validation.annotation.JsonValue;
import com.travis.infrastructure.framework.web.core.annotation.SanitizeHtml;
import com.travis.monolith.system.common.api.enums.Status;
import com.travis.monolith.system.message.api.enums.SysMessageChannel;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 更新消息模板请求参数。 */
@Data
public class SysMessageTemplateUpdateReq {
    @NotBlank(message = "模板名称不能为空")
    @Size(max = 100, message = "模板名称长度不能超过100个字符")
    private String templateName;

    @NotBlank(message = "推送通道不能为空")
    @EnumValue(value = SysMessageChannel.class, message = "推送通道错误")
    @Size(max = 32, message = "推送通道长度不能超过32个字符")
    private String channel;

    @Size(max = 255, message = "模板标题长度不能超过255个字符")
    private String title;

    @Size(max = 128, message = "平台模板ID长度不能超过128个字符")
    private String platformTemplateId;

    @Size(max = 4000, message = "字段结构长度不能超过4000个字符")
    @JsonValue(message = "字段结构必须是合法JSON对象")
    private String contentSchema;

    @SanitizeHtml
    @Size(max = 5000, message = "模板内容长度不能超过5000个字符")
    @NotBlank(message = "模板内容不能为空")
    private String content;

    @Size(max = 500, message = "跳转地址长度不能超过500个字符")
    private String redirectUrl;

    @NotNull(message = "状态不能为空")
    @EnumValue(value = Status.class, message = "状态值错误")
    private Integer status;

    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;

    @AssertTrue(message = "当前通道模板标题不能为空")
    public boolean isTitleValid() {
        return !SysMessageChannel.IN_APP.getValue().equals(channel)
                        && !SysMessageChannel.WECHAT_MP.getValue().equals(channel)
                        && !SysMessageChannel.WECHAT_OA.getValue().equals(channel)
                || StrUtil.isNotBlank(title);
    }

    @AssertTrue(message = "外部通道平台模板ID不能为空")
    public boolean isPlatformTemplateIdValid() {
        return !SysMessageChannel.isExternal(channel) || StrUtil.isNotBlank(platformTemplateId);
    }
}
