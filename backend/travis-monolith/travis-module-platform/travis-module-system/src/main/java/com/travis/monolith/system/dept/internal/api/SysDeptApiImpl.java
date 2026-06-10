package com.travis.monolith.system.dept.internal.api;

import com.travis.monolith.system.dept.api.SysDeptApi;
import com.travis.monolith.system.dept.api.response.SysDeptResp;
import com.travis.monolith.system.dept.internal.entity.SysDept;
import com.travis.monolith.system.dept.internal.service.SysDeptService;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 部门模块对外 API 实现，委托调用内部 Service
 *
 * @author travis
 */
@Component
@RequiredArgsConstructor
public class SysDeptApiImpl implements SysDeptApi {

    private final SysDeptService deptService;

    @Override
    public List<SysDeptResp> listTree() {
        return deptService.listTree();
    }

    @Override
    public Map<Long, String> getDeptNameMapByIds(Collection<Long> ids) {
        return deptService.getDeptNameMapByIds(ids);
    }

    @Override
    public String getDeptNameById(Long deptId) {
        return deptService.getDeptNameById(deptId);
    }

    @Override
    public boolean existsAnyByIds(Collection<Long> deptIds) {
        return deptIds != null
                && !deptIds.isEmpty()
                && deptService.lambdaQuery().in(SysDept::getId, deptIds).exists();
    }
}
