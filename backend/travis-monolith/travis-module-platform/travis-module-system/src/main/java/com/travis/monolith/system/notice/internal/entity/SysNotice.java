package com.travis.monolith.system.notice.internal.entity;

import com.travis.infrastructure.framework.mybatis.core.BaseEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 系统公告实体。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysNotice extends BaseEntity {
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
}
