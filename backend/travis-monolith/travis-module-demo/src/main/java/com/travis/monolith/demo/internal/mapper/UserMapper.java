package com.travis.monolith.demo.internal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travis.monolith.demo.internal.model.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口 继承BaseMapper后,自动拥有以下方法: - insert: 插入一条记录 - deleteById: 根据ID删除 - updateById: 根据ID更新 -
 * selectById: 根据ID查询 - selectList: 查询所有 - selectBatchIds: 根据ID批量查询 - selectCount: 查询总数 - 等等...
 */
@Mapper // 标识这是一个Mapper接口
public interface UserMapper extends BaseMapper<User> {
    // 如果只需要基本的CRUD,继承BaseMapper就够了,不需要写任何方法
    // 如果需要自定义复杂查询,可以在这里定义方法,然后在XML中写SQL
    // 例如:
    // List<User> selectCustomUsers(@Param("name") String name);
}
