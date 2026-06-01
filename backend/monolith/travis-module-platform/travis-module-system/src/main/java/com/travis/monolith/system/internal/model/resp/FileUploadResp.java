package com.travis.monolith.system.internal.model.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传响应
 *
 * @author travis
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResp {

    /**
     * 文件相对路径（用于数据库存储），如 /files/2026-06-02/abc.jpg
     */
    private String path;

    /**
     * 文件完整访问URL（用于前端展示），如 http://127.0.0.1/files/2026-06-02/abc.jpg
     */
    private String url;
}
