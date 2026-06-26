package com.travis.monolith.system.message.internal.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.travis.infrastructure.framework.mybatis.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 消息模板实体。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_message_template")
public class SysMessageTemplate extends BaseEntity {
    private String templateCode;
    private String templateName;
    private String channel;
    private String platformTemplateId;
    private String contentSchema;
    private String content;
    private String pagePath;
    private String jumpUrl;
    private Integer status;
    private String remark;
}
