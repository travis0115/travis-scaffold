package com.travis.monolith.system.message.internal.entity;

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

    /** 接收人登录体系，与 LoginType 常量取值保持一致。 */
    private String receiverType;

    private Long receiverId;
    private Integer readStatus;
    private LocalDateTime readTime;
}
