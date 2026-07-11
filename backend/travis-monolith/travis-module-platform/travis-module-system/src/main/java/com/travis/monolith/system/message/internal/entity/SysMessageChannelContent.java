package com.travis.monolith.system.message.internal.entity;

import com.travis.infrastructure.framework.mybatis.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 消息渠道内容实体。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysMessageChannelContent extends BaseEntity {
    private Long messageId;
    private String channel;
    private String title;
    private String subtitle;
    private String content;
    private String imageUrl;
    private String jumpUrl;
    private Long templateId;
    private String templateParams;
    private Integer wordCount;
}
