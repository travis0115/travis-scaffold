package com.travis.monolith.system.user.internal.event;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travis.monolith.system.dept.api.event.DeptDeletedEvent;
import com.travis.monolith.system.user.internal.entity.SysUser;
import com.travis.monolith.system.user.internal.mapper.SysUserMapper;
import java.nio.ByteBuffer;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.annotation.RocketMQMessageListener;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.apache.rocketmq.client.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 部门删除事件消费者，处理部门删除时清除关联用户的部门归属。 通过 RocketMQ 消费 {@code system-event:dept-deleted} 消息实现跨模块异步事件处理。
 *
 * @author travis
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "system-event",
        tag = "dept-deleted",
        consumerGroup = "system-dept-deleted-consumer")
@RequiredArgsConstructor
public class DeptEventListener implements RocketMQListener {

    private final SysUserMapper userMapper;
    private final ObjectMapper objectMapper;

    /** 消费部门删除事件，将关联用户的部门ID重置为 null */
    @Override
    @Transactional
    public ConsumeResult consume(MessageView messageView) {
        try {
            ByteBuffer buf = messageView.getBody();
            byte[] body = new byte[buf.remaining()];
            buf.get(body);
            DeptDeletedEvent event = objectMapper.readValue(body, DeptDeletedEvent.class);

            for (Long deptId : event.getDeptIds()) {
                List<SysUser> users =
                        userMapper.selectList(
                                new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeptId, deptId));
                for (SysUser user : users) {
                    user.setDeptId(null);
                    userMapper.updateById(user);
                }
            }
            return ConsumeResult.SUCCESS;
        } catch (Exception e) {
            log.error("消费部门删除事件失败", e);
            return ConsumeResult.FAILURE;
        }
    }
}
