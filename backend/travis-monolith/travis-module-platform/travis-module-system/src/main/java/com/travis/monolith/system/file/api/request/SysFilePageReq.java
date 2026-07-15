package com.travis.monolith.system.file.api.request;

import com.travis.infrastructure.common.web.model.PageRequest;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 文件分页查询参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysFilePageReq extends PageRequest {
    /** 所属文件夹 ID。 */
    private Long folderId;

    /** 系统文件名，支持模糊匹配。 */
    @Size(max = 255, message = "文件名长度不能超过255个字符")
    private String fileName;

    /** 上传时的原始文件名，支持模糊匹配。 */
    @Size(max = 500, message = "原文件名长度不能超过500个字符")
    private String originalName;

    /** MIME 类型。 */
    @Size(max = 255, message = "MIME类型长度不能超过255个字符")
    private String mimeType;

    /** 存储配置 ID。 */
    private Long storageConfigId;
}
