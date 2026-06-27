package com.travis.monolith.system.file.api.response;

import lombok.Builder;
import lombok.Data;

/**
 * 文件上传响应
 *
 * @author travis
 */
@Data
@Builder
public class FileUploadResp {

    /** 文件相对路径（用于数据库存储），如 /files/2026-06-02/abc.jpg */
    private String path;

    /** 文件完整访问URL（用于前端展示），如 http://127.0.0.1/files/2026-06-02/abc.jpg */
    private String url;

    /** 文件元数据ID */
    private Long id;
}
