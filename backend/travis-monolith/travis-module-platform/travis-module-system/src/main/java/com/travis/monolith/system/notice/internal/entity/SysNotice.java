package com.travis.monolith.system.notice.internal.entity;

import com.travis.infrastructure.framework.mybatis.core.BaseEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 系统公告实体。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysNotice extends BaseEntity {
    private String title;
    private String content;
    private Integer status;
    private LocalDateTime publishTime;
    private Integer isPinned;
    private Integer sort;
    private String remark;
}
