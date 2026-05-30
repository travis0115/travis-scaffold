package com.travis.infrastructure.framework.web.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    /**
     * 当前页码，从1开始
     */
    private Integer pageNum = 1;

    /**
     * 每页记录数
     */
    private Integer pageSize = 10;

    /**
     * 排序字段
     */
    private String orderBy;

    /**
     * 是否升序，默认true
     */
    private Boolean asc = true;

}
