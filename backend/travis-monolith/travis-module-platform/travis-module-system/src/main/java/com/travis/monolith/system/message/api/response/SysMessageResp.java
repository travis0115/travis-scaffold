package com.travis.monolith.system.message.api.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/** 消息推送详情响应对象。 */
@Data
public class SysMessageResp {
    private Long id;
    private String title;
    private String content;
    private Integer messageType;
    private Boolean hasTemplate;
    private Integer pushType;
    private String sourceType;
    private String sourceId;
    private String channel;
    private String jumpUrl;
    private Long templateId;
    private String templateParams;
    private Integer status;
    private String receiverType;
    private Integer receiverScope;
    private List<Long> receiverValues;
    private LocalDateTime publishTime;
    private String remark;
    private LocalDateTime createTime;
}
