package com.travis.infrastructure.framework.mybatis.core.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 扩展 MyBatis-Plus {@link BaseMapper}，提供额外便捷方法
 * <p>
 * 业务 Mapper 接口应继承此接口代替直接继承 {@link BaseMapper}。
 *
 * @param <T> 实体类型
 * @author travis
 */
public interface BaseMapperX<T> extends BaseMapper<T> {

    /**
     * 根据指定字段查询单条记录
     *
     * @param field 字段的 Lambda 引用
     * @param value 字段值
     * @return 匹配的单条记录，若无则返回 null
     */
    default T selectOne(SFunction<T, ?> field, Object value) {
        return selectOne(new LambdaQueryWrapper<T>().eq(field, value).last("LIMIT 1"));
    }

    /**
     * 根据指定字段查询记录列表
     *
     * @param field 字段的 Lambda 引用
     * @param value 字段值
     * @return 匹配的记录列表
     */
    default List<T> selectList(SFunction<T, ?> field, Object value) {
        return selectList(new LambdaQueryWrapper<T>().eq(field, value));
    }

    /**
     * 查询全部记录总数
     *
     * @return 记录总数
     */
    default Long selectCount() {
        return selectCount(new QueryWrapper<>());
    }

    /**
     * 查询全部记录
     *
     * @return 全部记录列表
     */
    default List<T> selectList() {
        return selectList(new QueryWrapper<>());
    }

    /**
     * 分页查询
     *
     * @param pageNum  页码（从1开始）
     * @param pageSize 每页大小
     * @param wrapper  查询条件
     * @return 分页结果
     */
    default Page<T> selectPage(int pageNum, int pageSize, LambdaQueryWrapper<T> wrapper) {
        return selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 无条件分页查询
     *
     * @param pageNum  页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页结果
     */
    default Page<T> selectPage(int pageNum, int pageSize) {
        return selectPage(new Page<>(pageNum, pageSize), new QueryWrapper<>());
    }
}
