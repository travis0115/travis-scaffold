package com.travis.monolith.system.message.internal.entity;

import com.travis.infrastructure.framework.mybatis.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 消息接收记录实体。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysMessageReceiver extends BaseEntity {
    private Long messageId;

    /** 接收人登录体系，与 LoginType 常量取值保持一致。 */
    private String receiverType;

    private Long receiverId;
    private Integer readStatus;
    private LocalDateTime readTime;
}
