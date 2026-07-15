package com.travis.monolith.system.dict.api.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字典类型响应视图
 *
 * @author travis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysDictResp {
    /** 字典 ID。 */
    private Long id;

    /** 字典名称。 */
    private String dictName;

    /** 字典编码。 */
    private String dictCode;

    /** 字典状态。 */
    private Integer status;

    /** 备注。 */
    private String remark;

    /** 排序号。 */
    private Integer sort;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 创建人 ID。 */
    private Long createBy;

    /** 更新时间。 */
    private LocalDateTime updateTime;

    /** 更新人 ID。 */
    private Long updateBy;

    /** 子节点（字典数据项列表），仅用于树形接口返回 */
    private List<SysDictItemResp> children;
}
