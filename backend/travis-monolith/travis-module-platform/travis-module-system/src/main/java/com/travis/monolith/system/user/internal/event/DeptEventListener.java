package com.travis.monolith.system.user.internal.event;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travis.infrastructure.framework.rocketmq.core.AbstractEventConsumer;
import com.travis.monolith.system.dept.api.event.DeptDeletedEvent;
import com.travis.monolith.system.user.internal.entity.SysUser;
import com.travis.monolith.system.user.internal.mapper.SysUserMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.client.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

/**
 * 部门删除事件消费者，处理部门删除时清除关联用户的部门归属。 通过 RocketMQ 消费 {@code system-event:dept-deleted} 消息实现跨模块异步事件处理。
 *
 * @author travis
 */
@Component
@RocketMQMessageListener(
        topic = "system-event",
        tag = "dept-deleted",
        consumerGroup = "system-dept-deleted-consumer")
@RequiredArgsConstructor
public class DeptEventListener extends AbstractEventConsumer<DeptDeletedEvent> {

    private final SysUserMapper userMapper;

    @Override
    protected void onEvent(DeptDeletedEvent payload) {
        for (Long deptId : payload.getDeptIds()) {
            List<SysUser> users =
                    userMapper.selectList(
                            new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeptId, deptId));
            for (SysUser user : users) {
                user.setDeptId(null);
                userMapper.updateById(user);
            }
        }
    }
}
