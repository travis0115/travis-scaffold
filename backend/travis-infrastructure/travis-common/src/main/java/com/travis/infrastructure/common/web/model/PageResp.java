package com.travis.infrastructure.common.web.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 统一分页响应
 *
 * @author travis
 */
@Data
@Builder
public class PageResp<T> implements Serializable {

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
