package com.travis.monolith.system.notice.internal.entity;

import com.travis.infrastructure.framework.mybatis.core.BaseEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 通知公告实体。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysNotice extends BaseEntity {
    private String title;
    private String content;
    private Integer noticeType;
    private Integer status;
    private Integer audienceType;
    private String targetIds;
    private LocalDateTime publishTime;
    private String remark;
}
