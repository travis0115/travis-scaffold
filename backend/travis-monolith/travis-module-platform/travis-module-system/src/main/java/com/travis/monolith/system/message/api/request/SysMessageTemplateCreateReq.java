package com.travis.monolith.system.message.api.request;

import com.travis.infrastructure.framework.web.core.annotation.SanitizeHtml;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SysMessageTemplateCreateReq {
    @NotBlank(message = "模板编码不能为空")
    @Size(max = 64, message = "模板编码长度不能超过64个字符")
    private String templateCode;

    @NotBlank(message = "模板名称不能为空")
    @Size(max = 100, message = "模板名称长度不能超过100个字符")
    private String templateName;

    @NotBlank(message = "推送通道不能为空")
    @Size(max = 32, message = "推送通道长度不能超过32个字符")
    private String channel;

    @Size(max = 64, message = "模板分类长度不能超过64个字符")
    private String templateType;

    @Size(max = 255, message = "模板标题长度不能超过255个字符")
    private String title;

    @Size(max = 128, message = "平台模板ID长度不能超过128个字符")
    private String platformTemplateId;

    @Size(max = 4000, message = "字段结构长度不能超过4000个字符")
    private String contentSchema;

    @SanitizeHtml
    @Size(max = 5000, message = "模板内容长度不能超过5000个字符")
    private String content;

    @Size(max = 500, message = "跳转地址长度不能超过500个字符")
    private String redirectUrl;

    @NotNull(message = "状态不能为空")
    private Integer status;

    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;
}
