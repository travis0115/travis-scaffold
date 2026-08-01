package com.travis.monolith.system.message.internal.entity;

import com.travis.infrastructure.framework.mybatis.core.BaseEntity;
import java.time.LocalDateTime;
import lombok.*;

/** 消息接收记录实体。 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SysMessageReceiver extends BaseEntity {
    /** 消息 ID。 */
    private Long messageId;

    /** 接收人登录体系，与 LoginType 常量取值保持一致。 */
    private String receiverType;

    /** 接收人 ID。 */
    private Long receiverId;

    /** 阅读状态。 */
    private Integer readStatus;

    /** 阅读时间。 */
    private LocalDateTime readTime;
}
