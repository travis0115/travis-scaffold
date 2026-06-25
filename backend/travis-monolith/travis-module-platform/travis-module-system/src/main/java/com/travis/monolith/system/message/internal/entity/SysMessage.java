package com.travis.monolith.system.message.internal.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.travis.infrastructure.framework.mybatis.core.BaseEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 消息推送实体。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_message")
public class SysMessage extends BaseEntity {
    private String title;
    private String content;
    private Integer messageType;
    private String sourceType;
    private String sourceId;
    private String channels;
    private Integer status;
    private Integer audienceType;
    private String targetIds;
    private LocalDateTime publishTime;
    private String remark;
}
