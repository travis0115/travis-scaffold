package com.travis.infrastructure.common.web.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一分页请求
 *
 * @author travis
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 当前页码，从1开始 */
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小值为 1")
    private Integer pageNum = 1;

    /** 每页记录数 */
    @NotNull(message = "每页条数不能为空")
    @Min(value = 1, message = "每页条数最小值为 1")
    @Max(value = 200, message = "每页条数最大值为 200")
    private Integer pageSize = 10;

    /** 排序字段 */
    private String orderBy;

    /** 是否升序，默认true */
    private Boolean asc = true;

}
