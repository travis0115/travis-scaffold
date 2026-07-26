package com.travis.monolith.system.message.api.response;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;

/** 用户收件箱消息详情响应。 */
@Data
public class SysUserMessageResp {
    /** 消息 ID。 */
    private Long messageId;

    /** 消息标题。 */
    private String title;

    /** 消息 HTML 内容。 */
    private String content;

    /** 消息类型。 */
    private Integer messageType;

    /** 业务来源类型。 */
    private String sourceType;

    /** 业务来源记录 ID。 */
    private String sourceId;

    /** 阅读状态。 */
    private Integer readStatus;

    /** 阅读时间。 */
    private LocalDateTime readTime;

    /** 消息发布时间。 */
    private LocalDateTime publishTime;

    /** 消息创建时间。 */
    private LocalDateTime createTime;

    /** 来源内容附加元数据。 */
    private Map<String, Object> metadata;
}
