package com.travis.infrastructure.common.mapstruct;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.travis.infrastructure.common.web.model.PageResp;

/**
 * @author travis
 */
public final class PageConverter {

    private PageConverter() {}

    public static <T> PageResp<T> toResp(IPage<T> page) {
        return PageResp.<T>builder()
                .records(page.getRecords())
                .total(page.getTotal())
                .pageNum(page.getCurrent())
                .pageSize(page.getSize())
                .totalPages(page.getPages())
                .build();
    }
}
