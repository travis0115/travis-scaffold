package com.travis.monolith.system.message.api.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/** 消息推送详情响应对象。 */
@Data
public class SysMessageResp {
    /** 消息 ID。 */
    private Long id;

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

    /** 消息模板渲染参数。 */
    private String templateParams;

    /** 消息状态。 */
    private Integer status;

    /** 接收端登录体系。 */
    private String receiverType;

    /** 接收范围。 */
    private Integer receiverScope;

    /** 接收范围对应的用户、角色或部门 ID 列表。 */
    private List<Long> receiverValues;

    /** 计划或实际发布时间。 */
    private LocalDateTime publishTime;

    /** 备注。 */
    private String remark;

    /** 创建时间。 */
    private LocalDateTime createTime;
}
