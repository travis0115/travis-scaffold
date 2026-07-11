package com.travis.monolith.system.message.internal.entity;

import com.baomidou.mybatisplus.annotation.TableName;
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
    private Boolean enableInboxCopy;
    private Integer status;
    private String receiverType;
    private Integer receiverScope;
    private String receiverValues;
    private LocalDateTime publishTime;
    private String remark;
}
