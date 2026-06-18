package com.travis.infrastructure.framework.mybatis.core;

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

    protected T getByIdOrThrow(Serializable id) {
        T entity = super.getById(id);
        if (entity == null) {
            throw new BizException(CommonErrorCode.DATABASE_SELECT_NOT_FOUND);
        }
        return entity;
    }

    protected T getOneOrThrow(LambdaQueryWrapperX<T> wrapper) {
        T entity = baseMapper.selectOne(wrapper.last("LIMIT 1"));
        if (entity == null) {
            throw new BizException(CommonErrorCode.DATABASE_SELECT_NOT_FOUND);
        }
        return entity;
    }

    protected Page<T> page(int pageNum, int pageSize, LambdaQueryWrapperX<T> wrapper) {
        return baseMapper.page(pageNum, pageSize, wrapper);
    }

    protected Page<T> page(int pageNum, int pageSize) {
        return baseMapper.page(pageNum, pageSize);
    }
}
