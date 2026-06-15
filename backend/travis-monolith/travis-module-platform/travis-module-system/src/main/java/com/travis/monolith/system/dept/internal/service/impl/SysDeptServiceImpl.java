package com.travis.monolith.system.dept.internal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.common.event.MessagePublisher;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.monolith.system.common.api.SystemErrorCode;
import com.travis.monolith.system.common.api.SystemEvent;
import com.travis.monolith.system.dept.api.event.DeptDeletedPayload;
import com.travis.monolith.system.dept.api.request.SysDeptCreateReq;
import com.travis.monolith.system.dept.api.request.SysDeptUpdateReq;
import com.travis.monolith.system.dept.api.response.SysDeptResp;
import com.travis.monolith.system.dept.internal.converter.SysDeptConverter;
import com.travis.monolith.system.dept.internal.entity.SysDept;
import com.travis.monolith.system.dept.internal.mapper.SysDeptMapper;
import com.travis.monolith.system.dept.internal.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 部门管理服务实现，支持树形部门结构的构建
 *
 * @author travis
 */
@Service
@RequiredArgsConstructor
@Slf4j
@CacheConfig(cacheNames = "sys_dept")
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept>
        implements SysDeptService {

    /** 对象转换器 */
    private final SysDeptConverter converter;

    /** 消息发布器（用于发布部门删除事件） */
    private final MessagePublisher messagePublisher;

    /** 获取部门树形列表 */
    @Override
    @Cacheable(key = "'tree:all'")
    public List<SysDeptResp> listTree() {
        var deptList = list(new LambdaQueryWrapperX<SysDept>().orderByAsc(SysDept::getSort));
        var deptRespList = converter.toRespList(deptList);
        return buildTree(deptRespList);
    }

    /** 根据部门ID列表批量获取部门名称映射 */
    @Override
    public Map<Long, String> getDeptNameMapByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return listByIds(ids).stream()
                .collect(Collectors.toMap(SysDept::getId, SysDept::getDeptName));
    }

    /** 获取部门详情 */
    @Override
    @Cacheable(key = "'id:'+#id")
    public SysDeptResp getById(Long id) {
        var dept = super.getById(id);
        if (dept == null) {
            throw new BizException(SystemErrorCode.DEPT_NOT_FOUND);
        }
        return converter.toResp(dept);
    }

    /** 根据部门ID查询部门名称 */
    @Override
    @Cacheable(key = "'name:'+#deptId")
    public String getDeptNameById(Long deptId) {
        if (deptId == null) {
            return null;
        }
        var dept = super.getById(deptId);
        if (dept == null) {
            throw new BizException(SystemErrorCode.DEPT_NOT_FOUND);
        }
        return dept.getDeptName();
    }

    @Override
    public boolean existsAnyByIds(Collection<Long> deptIds) {
        return deptIds != null
                && !deptIds.isEmpty()
                && lambdaQuery().in(SysDept::getId, deptIds).exists();
    }

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
    @CacheEvict(key = "'tree:all'")
    public void create(SysDeptCreateReq req) {
        var dept = converter.toEntity(req);
        save(dept);
    }

    /** 更新部门信息 */
    @Override
    @Caching(
            evict = {
                @CacheEvict(key = "'tree:all'"),
                @CacheEvict(key = "'id:'+#id"),
                @CacheEvict(key = "'name:'+#id")
            })
    @Transactional
    public void update(Long id, SysDeptUpdateReq req) {
        var dept = super.getById(id);
        if (dept == null) {
            throw new BizException(SystemErrorCode.DEPT_NOT_FOUND);
        }
        var parentId = req.getParentId();
        if (parentId != null && parentId != 0 && listSelfAndDescendantIds(id).contains(parentId)) {
            throw new BizException(SystemErrorCode.DEPT_PARENT_INVALID);
        }
        converter.update(req, dept);
        updateById(dept);
    }

    /** 删除部门（递归删除所有下级部门），通过事件通知用户模块清除关联 */
    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void deleteById(Long id) {
        var ids = new ArrayList<Long>();
        collectAllDescendantIds(id, ids);
        ids.add(id);
        removeBatchByIds(ids);
        var payload = new DeptDeletedPayload(ids);
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        messagePublisher.asyncPublish(
                                SystemEvent.DEPT_DELETED,
                                payload,
                                (event, body, options, ex) -> {
                                    if (ex != null) {
                                        log.error("部门删除事件发送失败, deptIds={}", payload.deptIds(), ex);
                                    }
                                });
                    }
                });
    }

    /**
     * 递归收集所有下级部门ID
     *
     * @param parentId 父部门ID
     * @param ids 收集结果
     */
    private void collectAllDescendantIds(Long parentId, List<Long> ids) {
        var childrenList =
                list(new LambdaQueryWrapperX<SysDept>().eq(SysDept::getParentId, parentId));
        for (SysDept child : childrenList) {
            ids.add(child.getId());
            collectAllDescendantIds(child.getId(), ids);
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
