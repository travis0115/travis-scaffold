package com.travis.monolith.system.file.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 文件存储类型枚举 */
@Getter
@AllArgsConstructor
public enum FileStorageType {
    /** 本地磁盘 */
    LOCAL("LOCAL", "本地存储"),

//    /** S3 兼容协议，后续可覆盖 MinIO、AWS S3 等 */
//    S3("S3", "S3 兼容存储"),
//
//    /** 阿里云 OSS */
//    ALIYUN_OSS("ALIYUN_OSS", "阿里云 OSS"),
//
//    /** 腾讯云 COS */
//    TENCENT_COS("TENCENT_COS", "腾讯云 COS"),
//
//    /** 七牛云 Kodo */
//    QINIU_KODO("QINIU_KODO", "七牛云 Kodo"),
//
//    /** 华为云 OBS */
//    HUAWEI_OBS("HUAWEI_OBS", "华为云 OBS"),
//
//    /** Azure Blob Storage */
//    AZURE_BLOB("AZURE_BLOB", "Azure Blob"),
//
//    /** Google Cloud Storage */
//    GOOGLE_CLOUD_STORAGE("GOOGLE_CLOUD_STORAGE", "Google Cloud Storage"),
;

    private final String value;

    private final String label;
}
