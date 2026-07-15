package com.travis.monolith.system.file.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 文件存储类型选项响应。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysFileStorageTypeResp {
    /** 存储类型展示名称。 */
    private String label;

    /** 存储类型值。 */
    private String value;
}
