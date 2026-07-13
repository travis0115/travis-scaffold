package com.travis.monolith.system.message.api.request;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/** 来源消息发布请求。 */
@Data
public class SysSourceMessagePublishReq {
    private Integer messageType;
    private String sourceType;
    private String sourceId;
    private String title;
    private String receiverType;
    private Integer receiverScope;
    private List<Long> receiverValues;
    private LocalDateTime publishTime;
    private boolean republish;
}
