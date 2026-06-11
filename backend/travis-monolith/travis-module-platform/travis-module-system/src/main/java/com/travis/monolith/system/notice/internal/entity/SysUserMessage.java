package com.travis.monolith.system.notice.internal.entity;

import com.travis.infrastructure.framework.mybatis.core.BaseEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserMessage extends BaseEntity {
    private Long noticeId;
    private Long userId;
    private Integer readStatus;
    private LocalDateTime readTime;
}
