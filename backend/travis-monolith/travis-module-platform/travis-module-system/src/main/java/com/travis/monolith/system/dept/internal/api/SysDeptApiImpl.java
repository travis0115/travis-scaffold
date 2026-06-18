package com.travis.monolith.system.dept.internal.api;

import com.travis.monolith.system.dept.api.SysDeptApi;
import com.travis.monolith.system.dept.api.response.SysDeptResp;
import com.travis.monolith.system.dept.internal.service.SysDeptService;
import java.util.Collection;
import java.util.LinkedHashMap;
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
    public List<SysDeptResp> listEnabledTree() {
        return deptService.listEnabledTree();
    }

    @Override
    public Map<Long, String> getDeptNameMapByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> result = new LinkedHashMap<>();
        for (Long id : ids) {
            var deptName = deptService.getDeptNameById(id);
            if (deptName != null) {
                result.put(id, deptName);
            }
        }
        return result;
    }

    @Override
    public String getDeptNameById(Long deptId) {
        return deptService.getDeptNameById(deptId);
    }

    @Override
    public boolean existsAnyByIds(Collection<Long> deptIds) {
        return deptService.existsAnyByIds(deptIds);
    }

    @Override
    public List<Long> listSelfAndDescendantIds(Long deptId) {
        return deptService.listSelfAndDescendantIds(deptId);
    }
}
