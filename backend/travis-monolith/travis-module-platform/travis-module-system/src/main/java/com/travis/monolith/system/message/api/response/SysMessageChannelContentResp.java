package com.travis.monolith.system.message.api.response;

import lombok.Data;

/** 消息渠道内容响应对象。 */
@Data
public class SysMessageChannelContentResp {
    private Long id;
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
