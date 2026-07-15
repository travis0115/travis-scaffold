package com.travis.monolith.system.message.internal.entity;

import com.travis.infrastructure.framework.mybatis.core.BaseEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 消息推送实体。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysMessage extends BaseEntity {
    /** 消息标题。 */
    private String title;

    /** 消息 HTML 内容。 */
    private String content;

    /** 消息类型。 */
    private Integer messageType;

    /** 推送方式。 */
    private Integer pushType;

    /** 业务来源类型。 */
    private String sourceType;

    /** 业务来源记录 ID。 */
    private String sourceId;

    /** 推送通道。 */
    private String channel;

    /** 点击消息后的跳转地址。 */
    private String jumpUrl;

    /** 消息模板 ID。 */
    private Long templateId;

    /** 消息模板渲染参数，使用 JSON 对象格式。 */
    private String templateParams;

    /** 消息状态。 */
    private Integer status;

    /** 接收端登录体系。 */
    private String receiverType;

    /** 接收范围。 */
    private Integer receiverScope;

    /** 序列化后的接收对象 ID 列表。 */
    private String receiverValues;

    /** 计划或实际发布时间。 */
    private LocalDateTime publishTime;

    /** 备注。 */
    private String remark;
}
