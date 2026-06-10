package com.travis.infrastructure.common.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 统一分页响应
 *
 * @author travis
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> implements Serializable {

    /** 数据列表 */
    private List<T> records;

    /** 总记录数 */
    private Long total;

    /** 当前页码 */
    private Long pageNum;

    /** 每页记录数 */
    private Long pageSize;

    /** 总页数 */
    private Long totalPages;
}
