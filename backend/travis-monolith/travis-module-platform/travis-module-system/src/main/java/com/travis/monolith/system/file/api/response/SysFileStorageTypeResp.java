package com.travis.monolith.system.file.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 文件存储类型选项响应。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysFileStorageTypeResp {
    private String label;
    private String value;
}
