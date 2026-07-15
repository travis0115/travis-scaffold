package com.travis.monolith.system.file.api.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/** 文件上传策略响应 */
@Data
@Builder
public class FileUploadPolicyResp {
    /** 允许上传的文件扩展名。 */
    private List<String> allowedExtensions;

    /** 单个文件允许的最大字节数。 */
    private Long maxFileSizeBytes;
}
