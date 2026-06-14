package com.travis.monolith.system.log.versionlog.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 系统版本日志修改请求参数
 *
 * @author travis
 */
@Data
public class SysVersionLogUpdateReq {
    /** 版本号（如 v1.0） */
    @NotBlank(message = "版本号不能为空")
    @Size(max = 50, message = "版本号长度不能超过50个字符")
    private String version;

    /** 更新标题 */
    @NotBlank(message = "更新标题不能为空")
    @Size(max = 200, message = "更新标题长度不能超过200个字符")
    private String title;

    /** 更新内容 */
    @NotBlank(message = "更新内容不能为空")
    private String content;

    /** 发布时间 */
    private LocalDateTime publishTime;

    /** 状态（0-草稿 1-已发布） */
    private Integer status;
}
