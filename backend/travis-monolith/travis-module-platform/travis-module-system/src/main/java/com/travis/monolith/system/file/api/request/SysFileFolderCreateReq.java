package com.travis.monolith.system.file.api.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SysFileFolderCreateReq {
    // 父文件夹ID（0 表示顶级文件夹）
    @NotNull(message = "父文件夹ID不能为空")
    private Long parentId;

    @NotBlank(message = "文件夹名称不能为空")
    @Size(max = 20, message = "文件夹名称长度不能超过20个字符")
    private String folderName;

    @NotNull(message = "排序号不能为空")
    @Min(value = 0, message = "排序号不能小于0")
    @Max(value = 9999, message = "排序号不能大于9999")
    private Integer sort;
}
