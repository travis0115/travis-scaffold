package com.travis.monolith.system.message.api.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class SysMessageDetailResp {
    private Long id;
    private String title;
    private String content;
    private Integer messageType;
    private Integer pushType;
    private String sourceType;
    private String sourceId;
    private String channels;
    private Integer status;
    private Integer audienceType;
    private List<Long> targetIds;
    private List<SysMessageChannelContentResp> channelContents;
    private LocalDateTime publishTime;
    private String remark;
    private LocalDateTime createTime;
}
