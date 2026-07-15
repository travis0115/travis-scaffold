package com.travis.monolith.system.version.api.response;

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
public class SysVersionResp {
    /** 版本记录 ID。 */
    private Long id;

    /** 版本号。 */
    private String version;

    /** 版本标题。 */
    private String title;

    /** 版本说明 HTML 内容。 */
    private String content;

    /** 发布时间。 */
    private LocalDateTime publishTime;

    /** 发布状态。 */
    private Integer status;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 创建人 ID。 */
    private Long createBy;

    /** 更新时间。 */
    private LocalDateTime updateTime;

    /** 更新人 ID。 */
    private Long updateBy;
}
