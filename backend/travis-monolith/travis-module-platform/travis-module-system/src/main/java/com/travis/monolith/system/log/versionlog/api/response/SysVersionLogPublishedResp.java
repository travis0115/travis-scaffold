package com.travis.monolith.system.log.versionlog.api.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统版本日志响应视图
 *
 * @author travis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysVersionLogPublishedResp {
    private Long id;
    private String version;
    private String title;
    private String content;
    private LocalDateTime publishTime;
    private Integer status;
    private LocalDateTime createTime;
    private Long createBy;
    private LocalDateTime updateTime;
    private Long updateBy;
}
