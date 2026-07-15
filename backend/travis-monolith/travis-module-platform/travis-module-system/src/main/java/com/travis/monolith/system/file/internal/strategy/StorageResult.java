package com.travis.monolith.system.file.internal.strategy;

/** 文件写入存储介质后的结果。 */
public record StorageResult(
        /** 文件相对存储路径。 */
        String path,
        /** 存储介质最终使用的文件名。 */
        String fileName) {}
