package com.travis.monolith.system.notice.api.response;

import java.time.LocalDateTime;
import lombok.Data;

/** 公告响应。 */
@Data
public class SysNoticeResp {
    /** 公告 ID。 */
    private Long id;

    /** 公告标题。 */
    private String title;

    /** 公告 HTML 内容。 */
    private String content;

    /** 发布状态。 */
    private Integer status;

    /** 发布时间。 */
    private LocalDateTime publishTime;

    /** 是否置顶。 */
    private Integer isPinned;

    /** 排序号。 */
    private Integer sort;

    /** 备注。 */
    private String remark;

    /** 创建时间。 */
    private LocalDateTime createTime;
}
