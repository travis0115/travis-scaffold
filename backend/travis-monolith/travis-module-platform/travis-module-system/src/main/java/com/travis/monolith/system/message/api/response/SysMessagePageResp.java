package com.travis.monolith.system.message.api.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/** 消息推送分页响应对象。 */
@Data
public class SysMessagePageResp {
    private Long id;
    private String title;
    private String content;
    private Integer messageType;
    private Integer pushType;
    private String sourceType;
    private String sourceId;
    private String channel;
    private Boolean enableInboxCopy;
    private Integer status;
    private String receiverType;
    private Integer receiverScope;
    private List<Long> receiverValues;
    private LocalDateTime publishTime;
    private String remark;
    private LocalDateTime createTime;
}
