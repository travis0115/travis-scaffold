package com.travis.monolith.system.file.api.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/** 文件上传策略响应 */
@Data
@Builder
public class FileUploadPolicyResp {
    private List<String> allowedExtensions;
    private Long maxFileSizeBytes;
}
