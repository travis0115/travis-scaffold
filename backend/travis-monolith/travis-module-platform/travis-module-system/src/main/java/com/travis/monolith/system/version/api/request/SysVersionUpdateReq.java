package com.travis.monolith.system.version.api.request;

import com.travis.infrastructure.framework.web.core.annotation.SanitizeHtml;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 系统版本日志修改请求参数
 *
 * @author travis
 */
@Data
public class SysVersionUpdateReq {
    /** 版本号（如 v1.0） */
    @NotBlank(message = "版本号不能为空")
    @Size(max = 50, message = "版本号长度不能超过50个字符")
    private String version;

    /** 更新标题 */
    @NotBlank(message = "更新标题不能为空")
    @Size(max = 200, message = "更新标w题长度不能超过200个字符")
    private String title;

    /** 更新内容 */
    @NotBlank(message = "更新内容不能为空")
    @Size(max = 5000, message = "更新内容长度不能超过5000个字符")
    @SanitizeHtml
    private String content;

    /** 发布时间 */
    @NotNull(message = "发布时间不能为空")
    private LocalDateTime publishTime;
}
