package com.travis.infrastructure.framework.mybatis.core.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * MyBatis-Plus 工具类
 * <p>
 * 提供分页构建、查询条件构建等辅助方法。
 *
 * @author travis
 */
public final class MyBatisUtils {

    private MyBatisUtils() {
        // 工具类禁止实例化
    }

    /**
     * 构建分页对象
     *
     * @param pageNum  页码（从1开始）
     * @param pageSize 每页大小
     * @param <T>      实体类型
     * @return 分页对象
     */
    public static <T> Page<T> buildPage(int pageNum, int pageSize) {
        return new Page<>(pageNum, pageSize);
    }

    /**
     * 构建不查询总数的分页对象（性能优化场景）
     *
     * @param pageNum  页码（从1开始）
     * @param pageSize 每页大小
     * @param <T>      实体类型
     * @return 分页对象
     */
    public static <T> Page<T> buildPageNoCount(int pageNum, int pageSize) {
        return new Page<>(pageNum, pageSize, false);
    }

}
