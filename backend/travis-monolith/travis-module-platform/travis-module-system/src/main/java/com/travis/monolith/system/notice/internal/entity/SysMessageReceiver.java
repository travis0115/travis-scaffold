package com.travis.monolith.system.notice.internal.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.travis.infrastructure.framework.mybatis.core.BaseEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_message_receiver")
public class SysMessageReceiver extends BaseEntity {
    private Long messageId;
    private String receiverType;
    private Long receiverId;
    private Integer readStatus;
    private LocalDateTime readTime;
}
