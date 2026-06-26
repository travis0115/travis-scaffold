package com.travis.monolith.system.message.api.request;

import com.travis.infrastructure.framework.web.core.annotation.SanitizeHtml;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SysMessageChannelContentReq {
    @NotBlank(message = "推送通道不能为空")
    private String channel;

    @Size(max = 255, message = "渠道标题长度不能超过255个字符")
    private String title;

    @Size(max = 255, message = "渠道副标题长度不能超过255个字符")
    private String subtitle;

    @SanitizeHtml
    @Size(max = 5000, message = "渠道内容长度不能超过5000个字符")
    private String content;

    @Size(max = 500, message = "大图URL长度不能超过500个字符")
    private String imageUrl;

    @Size(max = 500, message = "跳转链接长度不能超过500个字符")
    private String jumpUrl;

    private Long templateId;

    @Size(max = 4000, message = "模板参数长度不能超过4000个字符")
    private String templateParams;

    private Integer wordCount;
}
