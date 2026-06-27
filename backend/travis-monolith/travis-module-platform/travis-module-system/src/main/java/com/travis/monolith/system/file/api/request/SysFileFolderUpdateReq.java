package com.travis.monolith.system.file.api.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SysFileFolderUpdateReq {

    @NotBlank(message = "文件夹名称不能为空")
    @Size(max = 20, message = "文件夹名称长度不能超过20个字符")
    private String folderName;
}
