package com.travis.monolith.system.version.api.request;

import com.travis.infrastructure.framework.web.core.annotation.SanitizeHtml;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 系统版本修改请求参数
 *
 * @author travis
 */
@Data
public class SysVersionUpdateReq {
    /** 版本号（如 v1.0） */
    @NotBlank(message = "版本号不能为空")
    @Size(max = 50, message = "版本号长度不能超过50个字符")
    private String version;

    /** 版本标题 */
    @NotBlank(message = "版本标题不能为空")
    @Size(max = 255, message = "版本标题长度不能超过255个字符")
    private String title;

    /** 版本内容 */
    @NotBlank(message = "版本内容不能为空")
    @Size(max = 5000, message = "版本内容长度不能超过5000个字符")
    @SanitizeHtml
    private String content;
}
