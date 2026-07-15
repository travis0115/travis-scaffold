package com.travis.monolith.system.message.api.response;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;

/** 消息来源内容响应。 */
@Data
public class SysMessageSourceContentResp {
    /** 来源内容标题。 */
    private String title;

    /** 来源内容正文。 */
    private String content;

    /** 来源内容发布时间。 */
    private LocalDateTime publishTime;

    /** 来源内容附加元数据。 */
    private Map<String, Object> metadata;
}
