package com.travis.monolith.system.message.internal.entity;

import com.travis.infrastructure.framework.mybatis.core.BaseEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 消息推送实体。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysMessage extends BaseEntity {
    private String title;
    private String content;
    private Integer messageType;
    private Integer pushType;
    private String sourceType;
    private String sourceId;
    private String channel;
    private String jumpUrl;
    private Long templateId;
    private String templateParams;
    private Integer status;
    private String receiverType;
    private Integer receiverScope;
    private String receiverValues;
    private LocalDateTime publishTime;
    private String remark;
}
