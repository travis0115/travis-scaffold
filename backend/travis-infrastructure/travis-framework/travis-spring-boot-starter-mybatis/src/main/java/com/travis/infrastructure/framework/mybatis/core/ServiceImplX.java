package com.travis.infrastructure.framework.mybatis.core;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import java.io.Serializable;

/**
 * 扩展 MyBatis-Plus {@link ServiceImpl}，统一约束业务 Mapper 继承 {@link BaseMapperX}。
 *
 * @param <M> Mapper 类型
 * @param <T> 实体类型
 * @author travis
 */
public abstract class ServiceImplX<M extends BaseMapperX<T>, T> extends ServiceImpl<M, T> {

    protected M mapper() {
        return baseMapper;
    }

    /** 根据 ID 查询实体，不存在时抛出通用未找到异常。 */
    protected T getByIdOrThrow(Serializable id) {
        T entity = super.getById(id);
        if (entity == null) {
            throw new BizException(CommonErrorCode.DATABASE_RECORD_NOT_FOUND);
        }
        return entity;
    }

    /** 根据条件查询单个实体，不存在时抛出通用未找到异常。 */
    protected T getOneOrThrow(LambdaQueryWrapperX<T> wrapper) {
        T entity = getOne(wrapper);
        if (entity == null) {
            throw new BizException(CommonErrorCode.DATABASE_RECORD_NOT_FOUND);
        }
        return entity;
    }

    /** 根据条件查询单个实体，忽略多结果检查。 */
    protected T getOne(LambdaQueryWrapperX<T> wrapper) {
        return getOne((Wrapper<T>) wrapper);
    }

    /** 判断是否存在满足条件的实体。 */
    protected boolean exists(LambdaQueryWrapperX<T> wrapper) {
        return baseMapper.exists(wrapper);
    }

    @Override
    public T getOne(Wrapper<T> queryWrapper) {
        return getOne(queryWrapper, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getOne(Wrapper<T> queryWrapper, boolean throwEx) {
        if (queryWrapper instanceof LambdaQueryWrapperX<?> wrapper) {
            return baseMapper.selectOne(((LambdaQueryWrapperX<T>) wrapper).last("LIMIT 1"));
        }
        return super.getOne(queryWrapper, throwEx);
    }

    /** 根据页码、每页数量和查询条件执行分页查询。 */
    protected Page<T> page(int pageNum, int pageSize, LambdaQueryWrapperX<T> wrapper) {
        return baseMapper.page(pageNum, pageSize, wrapper);
    }

    /** 根据页码和每页数量执行无附加条件的分页查询。 */
    protected Page<T> page(int pageNum, int pageSize) {
        return baseMapper.page(pageNum, pageSize);
    }
}
