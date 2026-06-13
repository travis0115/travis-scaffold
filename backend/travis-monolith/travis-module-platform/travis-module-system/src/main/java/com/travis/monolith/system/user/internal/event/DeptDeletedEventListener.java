package com.travis.monolith.system.user.internal.event;

import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.rocketmq.core.AbstractEventListener;
import com.travis.monolith.system.common.api.SystemEventConstant;
import com.travis.monolith.system.dept.api.SysDeptApi;
import com.travis.monolith.system.dept.api.event.DeptDeletedPayload;
import com.travis.monolith.system.user.internal.entity.SysUser;
import com.travis.monolith.system.user.internal.service.SysUserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.client.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

/**
 * 部门删除事件消费者，处理部门删除时清除关联用户的部门归属。 通过 RocketMQ 消费 {@code system-normal-event:dept-deleted}
 * 消息实现跨模块异步事件处理。
 *
 * @author travis
 */
@Component
@RocketMQMessageListener(
        topic = SystemEventConstant.NORMAL_EVENT,
        tag = SystemEventConstant.DEPT_DELETED,
        consumerGroup = EventConsumerGroup.DEPT_DELETED_CONSUMER_GROUP)
@RequiredArgsConstructor
public class DeptDeletedEventListener extends AbstractEventListener<DeptDeletedPayload> {

    private final SysUserService sysUserService;
    private final SysDeptApi sysDeptApi;

    @Override
    protected void onEvent(DeptDeletedPayload payload) {
        if (sysDeptApi.existsAnyByIds(payload.deptIds())) {
            return;
        }
        for (Long deptId : payload.deptIds()) {
            List<SysUser> users =
                    sysUserService.list(
                            new LambdaQueryWrapperX<SysUser>().eq(SysUser::getDeptId, deptId));
            for (SysUser user : users) {
                user.setDeptId(null);
                sysUserService.updateById(user);
            }
        }
    }
}
