package com.travis.monolith.system.file.api.response;

import java.time.LocalDateTime;
import lombok.Data;

/** 文件信息响应。 */
@Data
public class SysFileResp {
    /** 文件 ID。 */
    private Long id;

    /** 所属文件夹 ID。 */
    private Long folderId;

    /** 存储配置 ID。 */
    private Long storageConfigId;

    /** 存储配置名称。 */
    private String storageConfigName;

    /** 存储类型。 */
    private String storageType;

    /** 上传人 ID。 */
    private Long createBy;

    /** 上传主体类型。 */
    private String uploaderType;

    /** 上传主体展示名称。 */
    private String uploaderName;

    /** 存储后的文件名。 */
    private String fileName;

    /** 上传时的原始文件名。 */
    private String originalName;

    /** 文件在存储介质中的相对路径。 */
    private String path;

    /** 文件访问地址。 */
    private String url;

    /** 文件扩展名。 */
    private String extension;

    /** MIME 类型。 */
    private String mimeType;

    /** 文件大小，单位字节。 */
    private Long size;

    /** 上传时间。 */
    private LocalDateTime createTime;
}
