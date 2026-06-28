package com.travis.monolith.system.user.internal.event;

import com.travis.monolith.system.dept.api.SysDeptApi;
import com.travis.monolith.system.dept.api.event.DeptDeletedEvent;
import com.travis.monolith.system.user.internal.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * 部门删除事件监听器，处理部门删除时清除关联用户的部门归属。
 *
 * @author travis
 */
@Component
@RequiredArgsConstructor
public class DeptDeletedEventListener {

    private final SysUserService sysUserService;
    private final SysDeptApi sysDeptApi;

    @ApplicationModuleListener
    void onDeptDeleted(DeptDeletedEvent event) {
        if (sysDeptApi.existsAnyByIds(event.deptIds())) {
            return;
        }
        sysUserService.resetDeptByDeptIds(event.deptIds());
    }
}
