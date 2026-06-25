package com.travis.monolith.system.notice.internal.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.travis.infrastructure.framework.mybatis.core.BaseEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 系统公告实体。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_announcement")
public class SysAnnouncement extends BaseEntity {
    private String title;
    private String content;
    private Integer status;
    private LocalDateTime publishTime;
    private LocalDateTime expireTime;
    private Integer pinned;
    private Integer sort;
    private String remark;
}
