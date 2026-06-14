package com.travis.monolith.system.file.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SysFileFolderUpdateReq {
    private Long parentId;

    @NotBlank(message = "文件夹名称不能为空")
    private String folderName;

    private Integer sort;
}
