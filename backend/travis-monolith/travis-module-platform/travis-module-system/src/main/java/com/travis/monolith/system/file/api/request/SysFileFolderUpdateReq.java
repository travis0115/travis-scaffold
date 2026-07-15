package com.travis.monolith.system.file.api.request;

import jakarta.validation.constraints.*;
import lombok.Data;

/** 文件夹更新参数。 */
@Data
public class SysFileFolderUpdateReq {

    /** 文件夹名称。 */
    @NotBlank(message = "文件夹名称不能为空")
    @Size(max = 20, message = "文件夹名称长度不能超过20个字符")
    private String folderName;
}
