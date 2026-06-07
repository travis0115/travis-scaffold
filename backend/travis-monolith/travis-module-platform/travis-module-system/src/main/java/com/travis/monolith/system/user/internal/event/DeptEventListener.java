package com.travis.monolith.system.user.internal.event;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travis.monolith.system.dept.api.event.DeptDeletedEvent;
import com.travis.monolith.system.user.internal.mapper.SysUserMapper;
import com.travis.monolith.system.user.internal.model.entity.SysUser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 部门事件监听器，处理部门删除时清除关联用户的部门归属
 *
 * @author travis
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeptEventListener {

    private final SysUserMapper userMapper;

    /**
     * 监听部门删除事件，将关联用户的部门ID重置为 null
     *
     * @param event 部门删除事件
     */
    @EventListener
    @Transactional
    public void onDeptDeleted(DeptDeletedEvent event) {
        for (Long deptId : event.getDeptIds()) {
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
