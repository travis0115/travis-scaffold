package com.travis.monolith.system.message.internal.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.travis.infrastructure.framework.mybatis.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 消息投放目标。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_message_target")
public class SysMessageTarget extends BaseEntity {
    private Long messageId;
    private Integer targetType;
    private Long targetId;
}
