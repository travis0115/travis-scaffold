package com.travis.monolith.system.message.api.response;

import java.time.LocalDateTime;
import lombok.Data;

/** 消息模板响应对象。 */
@Data
public class SysMessageTemplateResp {
    /** 模板 ID。 */
    private Long id;

    /** 乐观锁版本号。 */
    private Integer lockVersion;

    /** 模板编码。 */
    private String templateCode;

    /** 模板名称。 */
    private String templateName;

    /** 推送通道。 */
    private String channel;

    /** 模板标题。 */
    private String title;

    /** 外部平台模板 ID。 */
    private String platformTemplateId;

    /** 模板参数结构。 */
    private String contentSchema;

    /** 模板 HTML 内容。 */
    private String content;

    /** 点击消息后的跳转地址模板。 */
    private String redirectUrl;

    /** 模板状态。 */
    private Integer status;

    /** 是否为系统内置模板。 */
    private Integer isBuiltin;

    /** 备注。 */
    private String remark;

    /** 创建时间。 */
    private LocalDateTime createTime;
}
