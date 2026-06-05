package com.travis.monolith.demo.internal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.monolith.demo.internal.mapper.UserMapper;
import com.travis.monolith.demo.internal.model.User;
import com.travis.monolith.demo.internal.service.UserService;
import org.springframework.stereotype.Service;

/** 用户服务实现类 继承ServiceImpl后,自动拥有IService的所有方法实现 不需要写任何代码就能用 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    // 如果只需要基本的CRUD,继承ServiceImpl就够了,不需要写任何代码
    // 如果需要自定义业务方法,可以在这里实现
    // 例如:
    // @Override
    // public List<User> getActiveUsers() {
    //     return this.list(new LambdaQueryWrapper<User>().eq(User::getStatus, 1));
    // }
}
