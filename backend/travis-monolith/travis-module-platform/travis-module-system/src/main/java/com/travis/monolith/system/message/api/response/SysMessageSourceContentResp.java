package com.travis.monolith.system.message.api.response;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;

/** 消息来源内容响应。 */
@Data
public class SysMessageSourceContentResp {
    private String title;
    private String content;
    private LocalDateTime publishTime;
    private Map<String, Object> metadata;
}
