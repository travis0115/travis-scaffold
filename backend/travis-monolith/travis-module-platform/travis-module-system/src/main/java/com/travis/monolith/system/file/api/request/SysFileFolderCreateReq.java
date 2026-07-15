package com.travis.monolith.system.file.api.request;

import jakarta.validation.constraints.*;
import lombok.Data;

/** 文件夹创建参数。 */
@Data
public class SysFileFolderCreateReq {
    /** 父文件夹 ID，0 表示根目录。 */
    @NotNull(message = "父文件夹ID不能为空")
    private Long parentId;

    /** 文件夹名称。 */
    @NotBlank(message = "文件夹名称不能为空")
    @Size(max = 20, message = "文件夹名称长度不能超过20个字符")
    private String folderName;

    /** 排序号。 */
    @NotNull(message = "排序号不能为空")
    @Min(value = 0, message = "排序号不能小于0")
    @Max(value = 9999, message = "排序号不能大于9999")
    private Integer sort;
}
