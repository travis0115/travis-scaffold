package com.travis.monolith.system.message.api.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class SysMessagePageResp {
    private Long id;
    private String title;
    private String content;
    private Integer messageType;
    private Integer pushType;
    private String sourceType;
    private String sourceId;
    private String channels;
    private Integer status;
    private String receiverType;
    private Integer receiverScope;
    private List<Long> receiverValues;
    private LocalDateTime publishTime;
    private String remark;
    private LocalDateTime createTime;
}
