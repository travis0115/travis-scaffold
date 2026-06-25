package com.travis.monolith.system.notice.api.request;

import com.travis.infrastructure.framework.web.core.annotation.SanitizeHtml;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SysNoticeUpdateReq {
    @NotBlank(message = "公告标题不能为空")
    @Size(max = 200, message = "公告标题长度不能超过200个字符")
    private String title;

    @NotBlank(message = "公告内容不能为空")
    @SanitizeHtml
    @Size(max = 5000, message = "公告内容长度不能超过5000个字符")
    private String content;

    private LocalDateTime publishTime;
    private Integer pinned;
    private Integer sort;
    private String remark;
}
