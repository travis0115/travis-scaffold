package com.travis.monolith.system.message.api.request;

import com.travis.infrastructure.framework.web.core.annotation.SanitizeHtml;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class SysMessageUpdateReq {
    @NotBlank(message = "消息标题不能为空")
    private String title;

    @NotBlank(message = "消息内容不能为空")
    @SanitizeHtml
    @Size(max = 5000, message = "消息内容长度不能超过5000个字符")
    private String content;

    @NotNull(message = "消息类型不能为空")
    private Integer messageType;

    @NotNull(message = "推送方式不能为空")
    private Integer pushType;

    private String sourceType;
    private String sourceId;
    private String channels;

    /** 接收范围：0-全部用户 1-指定用户 2-指定角色 3-指定部门 */
    @NotNull(message = "接收范围不能为空")
    private Integer audienceType;

    private List<Long> targetIds;
    private List<SysMessageChannelContentReq> channelContents;

    private LocalDateTime publishTime;
    private String remark;
}
