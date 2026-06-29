package com.travis.infrastructure.framework.mybatis.core;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;

/**
 * 扩展 MyBatis-Plus {@link BaseMapper}，提供额外便捷方法
 *
 * <p>业务 Mapper 接口应继承此接口代替直接继承 {@link BaseMapper}。
 *
 * @param <T> 实体类型
 * @author travis
 */
public interface BaseMapperX<T> extends BaseMapper<T> {

    /**
     * 查询全部记录总数
     *
     * @return 记录总数
     */
    default Long countAll() {
        return selectCount(new QueryWrapperX<>());
    }

    /**
     * 查询全部记录
     *
     * @return 全部记录列表
     */
    default List<T> listAll() {
        return selectList(new QueryWrapperX<>());
    }

    /**
     * 分页查询
     *
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @param wrapper 查询条件
     * @return 分页结果
     */
    default Page<T> page(int pageNum, int pageSize, LambdaQueryWrapperX<T> wrapper) {
        return selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 无条件分页查询
     *
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页结果
     */
    default Page<T> page(int pageNum, int pageSize) {
        return selectPage(new Page<>(pageNum, pageSize), new QueryWrapperX<>());
    }
}
