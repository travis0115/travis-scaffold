package com.travis.monolith.demo.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.monolith.demo.internal.model.User;

/**
 * 用户服务接口 继承IService后,自动拥有以下方法: - save: 保存一条记录 - saveBatch: 批量保存 - removeById: 根据ID删除 - updateById:
 * 根据ID更新 - getById: 根据ID查询 - list: 查询所有 - page: 分页查询 - count: 查询总数 - 等等...
 */
public interface UserService extends IService<User> {
    // 如果只需要基本的CRUD,继承IService就够了
    // 如果需要自定义业务方法,可以在这里定义
    // 例如:
    // List<User> getActiveUsers();
}
