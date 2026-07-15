package com.travis.monolith.system.file.internal.entity;

import com.travis.infrastructure.framework.mybatis.core.BaseEntity;
import lombok.*;

/** 文件元数据实体。 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SysFile extends BaseEntity {
    /** 所属文件夹 ID。 */
    private Long folderId;

    /** 存储配置 ID。 */
    private Long storageConfigId;

    /** 上传主体类型。 */
    private String uploaderType;

    /** 上传主体展示名称快照。 */
    private String uploaderName;

    /** 存储后的文件名。 */
    private String fileName;

    /** 上传时的原始文件名。 */
    private String originalName;

    /** 文件在存储介质中的相对路径。 */
    private String path;

    /** 文件扩展名。 */
    private String extension;

    /** MIME 类型。 */
    private String mimeType;

    /** 文件大小，单位字节。 */
    private Long size;
}
