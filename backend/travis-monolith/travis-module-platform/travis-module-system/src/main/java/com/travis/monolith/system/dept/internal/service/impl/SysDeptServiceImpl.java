package com.travis.monolith.system.dept.internal.service.impl;

import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.monolith.system.common.api.enums.Status;
import com.travis.monolith.system.common.api.enums.SystemErrorCode;
import com.travis.monolith.system.dept.api.event.DeptDeletedEvent;
import com.travis.monolith.system.dept.api.request.SysDeptCreateReq;
import com.travis.monolith.system.dept.api.request.SysDeptUpdateReq;
import com.travis.monolith.system.dept.api.response.SysDeptResp;
import com.travis.monolith.system.dept.internal.converter.SysDeptConverter;
import com.travis.monolith.system.dept.internal.entity.SysDept;
import com.travis.monolith.system.dept.internal.mapper.SysDeptMapper;
import com.travis.monolith.system.dept.internal.service.SysDeptService;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 部门管理服务实现，支持树形部门结构的构建
 *
 * @author travis
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "system:dept")
public class SysDeptServiceImpl extends ServiceImplX<SysDeptMapper, SysDept>
        implements SysDeptService {

    /** 对象转换器 */
    private final SysDeptConverter converter;

    private final ApplicationEventPublisher eventPublisher;

    /** 获取部门树形列表 */
    @Override
    @Cacheable(key = "'tree:all'")
    public List<SysDeptResp> listTree() {
        var deptList = list(new LambdaQueryWrapperX<SysDept>().orderByAsc(SysDept::getSort));
        var deptRespList = converter.toRespList(deptList);
        return buildTree(deptRespList);
    }

    /** 获取启用部门树形列表 */
    @Override
    @Cacheable(key = "'tree:enabled'")
    public List<SysDeptResp> listEnabledTree() {
        var deptList =
                list(
                        new LambdaQueryWrapperX<SysDept>()
                                .eq(SysDept::getStatus, Status.ENABLED.getValue())
                                .orderByAsc(SysDept::getSort));
        var deptRespList = converter.toRespList(deptList);
        return buildTree(deptRespList);
    }

    /** 获取部门详情 */
    @Override
    @Cacheable(key = "'detail:'+#id")
    public SysDeptResp getDetailByIdOrThrow(Long id) {
        var dept = getByIdOrThrow(id);
        return converter.toResp(dept);
    }

    /** 根据部门ID查询部门名称 */
    @Override
    @Cacheable(key = "'name:'+#deptId")
    public String getDeptNameByIdOrThrow(Long deptId) {
        if (deptId == null || deptId == 0) {
            return null;
        }
        var dept = getByIdOrThrow(deptId);
        return dept.getDeptName();
    }

    /** 批量查询部门 ID 与部门名称。 */
    @Override
    public Map<Long, String> getDeptNameMapByIds(Collection<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return Map.of();
        }
        return list(
                        new LambdaQueryWrapperX<SysDept>()
                                .select(SysDept::getId, SysDept::getDeptName)
                                .in(SysDept::getId, deptIds))
                .stream()
                .collect(
                        Collectors.toMap(
                                SysDept::getId,
                                SysDept::getDeptName,
                                (left, right) -> left,
                                LinkedHashMap::new));
    }

    /** 判断指定 ID 集合中是否存在部门。 */
    @Override
    public boolean existsAnyByIds(Collection<Long> deptIds) {
        return deptIds != null
                && !deptIds.isEmpty()
                && lambdaQuery().in(SysDept::getId, deptIds).exists();
    }

    /** 查询指定部门及其全部下级部门 ID。 */
    @Override
    public List<Long> listSelfAndDescendantIds(Long deptId) {
        if (deptId == null) {
            return List.of();
        }
        var departmentList =
                list(
                        new LambdaQueryWrapperX<SysDept>()
                                .select(SysDept::getId, SysDept::getParentId));
        Set<Long> result = new HashSet<>();
        result.add(deptId);
        boolean changed;
        do {
            changed = false;
            for (SysDept department : departmentList) {
                if (result.contains(department.getParentId()) && result.add(department.getId())) {
                    changed = true;
                }
            }
        } while (changed);
        return List.copyOf(result);
    }

    /** 新增部门 */
    @Override
    @Transactional
    @Caching(evict = {@CacheEvict(key = "'tree:all'"), @CacheEvict(key = "'tree:enabled'")})
    public void create(SysDeptCreateReq req) {
        validateParent(null, req.getParentId());
        var dept = converter.toEntity(req);
        save(dept);
    }

    /** 更新部门信息 */
    @Override
    @Caching(
            evict = {
                @CacheEvict(key = "'tree:all'"),
                @CacheEvict(key = "'tree:enabled'"),
                @CacheEvict(key = "'detail:'+#id"),
                @CacheEvict(key = "'name:'+#id"),
                @CacheEvict(value = "system:user:detail", allEntries = true)
            })
    @Transactional
    public void update(Long id, SysDeptUpdateReq req) {
        var dept = baseMapper.selectByIdForUpdate(id);
        if (dept == null) {
            throw new BizException(CommonErrorCode.DATABASE_RECORD_NOT_FOUND);
        }
        validateParent(id, req.getParentId());
        converter.update(req, dept);
        updateById(dept);
    }

    /** 修改部门状态 */
    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(key = "'tree:all'"),
                @CacheEvict(key = "'tree:enabled'"),
                @CacheEvict(key = "'detail:'+#id"),
                @CacheEvict(key = "'name:'+#id")
            })
    public void updateStatus(Long id, Integer status) {
        var dept = getByIdOrThrow(id);
        dept.setStatus(status);
        updateById(dept);
    }

    /** 删除部门（递归删除所有下级部门），通过事件通知用户模块清除关联 */
    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void deleteById(Long id) {
        if (baseMapper.selectByIdForUpdate(id) == null) {
            throw new BizException(CommonErrorCode.DATABASE_RECORD_NOT_FOUND);
        }
        var ids = listSelfAndDescendantIds(id);
        removeBatchByIds(ids);
        eventPublisher.publishEvent(new DeptDeletedEvent(ids));
    }

    private void validateParent(Long id, Long parentId) {
        if (Objects.equals(parentId, 0L)) {
            return;
        }
        if (parentId == null
                || baseMapper.selectByIdForUpdate(parentId) == null
                || (id != null && listSelfAndDescendantIds(id).contains(parentId))) {
            throw new BizException(SystemErrorCode.DEPT_PARENT_INVALID);
        }
    }

    /**
     * 根据 parentId 分组，将子节点挂到对应的父节点上，返回顶层节点（parentId=0）
     *
     * @param all 所有部门视图列表
     * @return 顶层部门树
     */
    private List<SysDeptResp> buildTree(List<SysDeptResp> all) {
        var grouped = all.stream().collect(Collectors.groupingBy(SysDeptResp::getParentId));
        all.forEach(
                node -> node.setChildren(grouped.getOrDefault(node.getId(), new ArrayList<>())));
        return all.stream().filter(node -> node.getParentId() == 0).collect(Collectors.toList());
    }
}
